package com.tonbu.web.app.itsm.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.action.BaseAction;
import com.tonbu.web.app.itsm.pojo.*;
import com.tonbu.web.app.itsm.service.ImportantPojoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import oracle.jdbc.proxy.annotation.Post;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping(value = "/api/v1/important/")
@Api(tags = "要事登记")
public class ApiImportantAction  extends BaseAction {

    Logger logger = LogManager.getLogger(ApiImportantAction.class.getName());

    @Autowired
    HttpServletRequest request;


    @Autowired
    ImportantPojoService importantPojoService;



    /**
     * 要事登记--list
     *
     * @return
     */

    @ApiOperation(value = "要事登记list查询")
    @ApiImplicitParams({
//            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "limit", value = "一页条数", required = true),
            @ApiImplicitParam(dataType = "string", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),
//            @ApiImplicitParam(dataType = "string", name = "saveorsubmit", value = "保存或提交（0,1）", required = true),


    })
    @PostMapping(value = "/user/ysdj/list")
    @ResponseBody
    public Object ysdjList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        Page<?> page;
        try {
            page = importantPojoService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 要事登记--byid
     *
     * @return
     */

    @ApiOperation(value = "要事登记map查询")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "主键", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "用户id", required = true)

    })
    @PostMapping(value = "/user/ysdj/listbyid")
    @ResponseBody
    public Object ysdjById() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            Map<String, Object> map = importantPojoService.getById(param);
            List<Map<String,Object>> list = new ArrayList<>();
            list.add(map);
            r.setDataset(list);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @ApiOperation(value = "要事登记新增保存")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),
            @ApiImplicitParam(dataType = "string", name = "saveorsubmit", value = "保存或提交（0,1）", required = true),
            @ApiImplicitParam(dataType = "string", name = "jsondata", value = "jsondata", required = true)
    })
    @PostMapping(value = "/user/ysdj/save")
    @ResponseBody
    public Object saveWtfb() {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            //获取登录用户信息
            UserEntity u =CustomRealm.GetMobileUser();
            //提交还是保存
            String saveorsubmit = param.get("saveorsubmit");
            //json字符串转java对象
            JSONObject userJson = JSONObject.parseObject(param.get("jsondata"));
            Important important = JSON.toJavaObject(userJson, Important.class);
            //新增从seq获取id
            String id = DBHelper.queryForScalar("SELECT SEQ_YSDJ.NEXTVAL FROM DUAL", Integer.class).toString();
            //获取登录用户，tb_user表信息
            Map<String, Object> usermap = importantPojoService.getUserInfoObjectMap(param);
            //新增
            if(trimToEmpty(important.getId()).isEmpty()){
                //拿到前台获取的时间
                String jsondata = param.get("jsondata");
                /*String s = new String();
                HashMap<String, Object> map = new HashMap<>();
                */
                Map<String,Object> map = json2map(jsondata);
                String time =(String)map.get("datetime");

                //拿到今天的时间
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                String testDate=df.format(new Date());//格式化当前日期

                //判断前台时间是否为为将来时间
                if (df.parse(time).getTime()>df.parse(testDate).getTime()){
                    r.setCode(-1);
                    r.setMsg("时间错误");
                    return r;
                }
                    //查询选中时间是否已经新增过
                String TB_YSDJ_Info = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)= ? and y.create_ss_id=?" , String.class,time,usermap.get("SS_ID"));

                if(!trimToEmpty(TB_YSDJ_Info).equals("")){
                    r.setCode(-1);
                    r.setMsg("已新增过");
                    return r;
                }




                //新增设置id
                important.setId(id);
                //要事登记主表保存
                DBHelper.save(important);
                //查铺查哨遍历新增
                if (important.getCpcs()!=null){
                    List<Cpcs> cpcsList = important.getCpcs();
                    cpcsList.stream().forEach(item->{
                        //查铺查哨 主键
                        item.setId(DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", Integer.class).toString());
                        //查铺查哨 要事登记主键
                        item.setYsdj_id(important.getId());
                        DBHelper.save(item);
                    });
                }
                //
                if (important.getLsldqs()!=null){
                    List<Lsldqs> lsldqsList = important.getLsldqs();
                    lsldqsList.stream().forEach(item->{
                        item.setId(DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", Integer.class).toString());
                        item.setYsdj_id(important.getId());
                        DBHelper.save(item);
                    });
                }

                //根据请假信息的单位信息，更新ysdj主键
                DBHelper.execute("update TB_YSDJ_LEAVE set ysdj_id=?  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  = ?",important.getId(),usermap.get("DWMC_TRUE"),time);

                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //单独更新要事登记主表 创建人、创建时间
                DBHelper.execute("update TB_YSDJ set CREATE_SS_ID=?,CREATE_DATE=?  where ID=? ",u.getUser_id(),sf.format(new Date()),important.getId());
            }else{
                //修改
                // 要事登记主表
                DBHelper.modify(important);
                //删除关联的查铺查哨信息 ，后面再新增，避免修改
                DBHelper.execute("delete from  TB_YSDJ_CPCS  where YSDJ_ID=? ",important.getId());

                //查铺查哨
                if (important.getCpcs()!=null){
                    List<Cpcs> cpcsList = important.getCpcs();
                    cpcsList.stream().forEach(item->{
                        if (trimToEmpty(item.getId()).equals("")) {
                            item.setId(DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", Integer.class).toString());
                        }
                        item.setYsdj_id(important.getId());
                        DBHelper.save(item);
                    });
                }
                DBHelper.execute("delete from  TB_YSDJ_LSLDQS  where YSDJ_ID=? ",important.getId());

                //
                if (important.getLsldqs()!=null){
                    List<Lsldqs> lsldqsList = important.getLsldqs();
                    lsldqsList.stream().forEach(item->{
                        if (trimToEmpty(item.getId()).equals("")){
                            item.setId(DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", Integer.class).toString());
                        }
                        item.setYsdj_id(important.getId());
                        DBHelper.save(item);
                    });
                }
            }

            if (saveorsubmit.equals("0")){
                //保存
                DBHelper.execute("update TB_YSDJ set status=0  where ID=? and (status<>1 or status is null )",important.getId());

            }else if(saveorsubmit.equals("1")){
                //修改
                DBHelper.execute("update TB_YSDJ set status=1  where ID=?",important.getId());
            }

            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /**
     * 要事登记--byid
     *
     * @return
     */

    @ApiOperation(value = "要事登记leave查询")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "userId", value = "用户id", required = true)

    })
    @PostMapping(value = "/user/ysdj/leaveinfo")
    @ResponseBody
    public Object leaveInfo() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
//            保存中队请假信息
            importantPojoService.saveLeave(param);
            List<Map<String, Object>> list = importantPojoService.getByIdForLeave(param);
            r.setDataset(list);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /**
     * json字符串转成map的
     *
     * @param str_json
     * @return
     */
    public static Map<String, Object> json2map(String str_json) {
        Map<String, Object> res = null;
        try {
            Gson gson = new Gson();
            res = gson.fromJson(str_json, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
        }
        return res;
    }




}