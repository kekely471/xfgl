package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.MapUtils;
import com.tonbu.web.admin.service.ImportService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tonbu.web.admin.action.BaseAction.trimToEmpty;

/*
*人员分析中的要事登记
 */
@RequestMapping(value = "/import")
@Controller
public class ImportAction {
    Logger logger = LogManager.getLogger(ImportAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    ImportService importService;

    /*
    要事登记的首页
     */
    @RequestMapping(value = "/")
    public ModelAndView home(){

        ModelAndView view = new ModelAndView("admin/manage/import/list");
        return view;
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = importService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }finally {
            return r;
        }

    }


    /*
    新增编辑
     */
    @RequestMapping(value = {"/add/{id}","/add/"})
    public ModelAndView add(@PathVariable(required = false) String id){
        ModelAndView view = new ModelAndView("admin/manage/import/add");
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        if (StringUtils.isEmpty(id)){
            UserEntity u = CustomRealm.GetLoginUser();
            Map<String,Object> m = new HashMap<>();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            String testDate=df.format(new Date());//格式化当前日期
            m.put("DATETIME",testDate);
            //后台获取周几
            Date date = new Date();
            SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
            m.put("WEEK",dateFm.format(date));

            importService.saveLeave(param);
            List<Map<String, Object>> list = importService.getByIdForLeave(param);
            m.put("leave",list);
            view.addObject("YSDJ",m);
            return view;
        }
        try{
            Map<String, Object> map = DBHelper.queryForMap("select t.ID " +
                    " ,t.DATETIME " +
                    " ,t.WEEK " +
                    " ,t.WEATHER " +
                    " ,t.ZBY " +
                    " ,t.BZ_GB_COUNT " +
                    " ,t.BZ_XFY_COUNT " +
                    " ,t.BZ_COUNT " +
                    " ,t.XY_GB_COUNT " +
                    " ,t.XY_XFY_COUNT " +
                    " ,t.XY_COUNT " +
                    " ,t.RWQK_ZC_NR " +
                    " ,t.RWQK_ZC_YD_COUNT " +
                    " ,t.RWQK_ZC_SD_COUNT " +
                    " ,t.RWQK_ZC_PERCENT " +
                    " ,t.RWQK_SW_NR " +
                    " ,t.RWQK_SW_YD_COUNT " +
                    " ,t.RWQK_SW_SD_COUNT " +
                    " ,t.RWQK_SW_PERCENT " +
                    " ,t.RWQK_XW_NR " +
                    " ,t.RWQK_XW_YD_COUNT " +
                    " ,t.RWQK_XW_SD_COUNT " +
                    " ,t.RWQK_XW_PERCENT " +
                    " ,t.RWQK_WS_NR " +
                    " ,t.RWQK_WS_YD_COUNT " +
                    " ,t.RWQK_WS_SD_COUNT " +
                    " ,t.RWQK_WS_PERCENT " +
                    " ,t.GCQW " +
                    " ,t.RYZBBD " +
                    " ,t.FJJCQK " +
                    " ,t.PBQK_ZBY_JIAOBZ " +
                    " ,t.PBQK_ZBY_JIEBZ " +
                    " ,t.PBQK_ZRY_JIAOBZ " +
                    " ,t.PBQK_ZRY_JIEBZ " +
                    " ,t.PBQK_CFZBY_JIAOBZ " +
                    " ,t.PBQK_CFZBY_JIEBZ " +
                    " ,t.PBQK_JJ_QK " +
                    " ,t.PBQK_JJ_DATE " +
                    " ,t.PBQK_ZZZ " +
                    " ,t.ZYSX " +
                    " ,t.STATUS " +
                    " ,t.CREATE_SS_ID " +
                    " ,t.CREATE_DATE " +
                    " ,t.BHJCLQK " +
                    " ,d.DEPTNAME " +
                    " ,d.id as DEPTID " +
                    "  from TB_YSDJ t" +
                    "  left join tb_user u on u.ss_id = t.CREATE_SS_ID " +
                    "  left join ss_dept d on d.id = u.dwmc " +
                    "  where t.id = ? " +
                    "  order by t.CREATE_DATE desc  ",id);
            if (map.size()!=0){

                List<Map<String,Object>> TB_YSDJ_CPCS=DBHelper.queryForList("select  id,ysdj_id,jcr,datetime,lby,jwry,jcqk  from TB_YSDJ_CPCS  where ysdj_id=?",id);
                if(TB_YSDJ_CPCS.size()>0){
                    map.put("cpcs", MapUtils.listformLowerCase(TB_YSDJ_CPCS));}
                List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where ysdj_id=?",id);
                if(TB_YSDJ_LEAVE.size()>0){
                    map.put("leave",MapUtils.listformLowerCase(TB_YSDJ_LEAVE));}
                List<Map<String,Object>> TB_YSDJ_LSLDQS=DBHelper.queryForList("select  id,ysdj_id,xfyxm,qsxm,gx,time_laid,time_lid from TB_YSDJ_LSLDQS  where ysdj_id=? ",id);
                if(TB_YSDJ_LSLDQS.size()>0){
                    map.put("lsldqs",MapUtils.listformLowerCase(TB_YSDJ_LSLDQS));}
            }
            /*
                        Map<String, Object> map = importService.getById(param);

             */
            view.addObject("YSDJ",map);
            view.addObject("id",id);
            Integer count = DBHelper.queryForScalar("select  COUNT(*)  from TB_YSDJ_CPCS  where ysdj_id= ?", Integer.class, id);
            view.addObject("count",count);

            return view;
        }catch (ActionException ex){
            throw ex;
        }
    }
    //保存
    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save(){
       ResultEntity r = new ResultEntity(1);
       try{
           //整合参数
           Map<String, String> param = RequestUtils.toMap(request);
           UserEntity userEntity = CustomRealm.GetLoginUser();
           String userId = userEntity.getUser_id();
           param.put("userId",userId);
           //查询今天当前用户是否已经新增过
           String id = param.get("id");
           if (StringUtils.isBlank(id)){


               String time = param.get("time");
               /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
               String testDate=df.format(new Date());//格式化当前日期*/

               String TB_YSDJ_Info = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)= ? and y.create_ss_id=?  " , String.class,time,userId);
               if(!trimToEmpty(TB_YSDJ_Info).equals("")){
                   r.setCode(-1);
                   r.setMsg("已新增过");
                   return r;
               }


           }

           importService.save(param);
           r.setMsg("操作成功");
       }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            throw ex;
       }finally {
           return r;
       }
    }

    @RequestMapping(value = "/tj")
    @ResponseBody
    public Object tj(){
        ResultEntity r = new ResultEntity(1);
        try{
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser();
            String userId = userEntity.getUser_id();
            param.put("userId",userId);
            //查询今天当前用户是否已经新增过
           /* String TB_YSDJ_Info = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)=to_char(sysdate,'yyyy-MM-dd') and y.create_ss_id=? and y.status=1 " , String.class,userId);
            int count = DBHelper.execute("select * from TB_YSDJ where DATETIME= ? ", param.get("datetime"));
            if (count>1){
                r.setCode(-1);
                r.setMsg("当前日期已提交过");
                return r;
            }
            if(!trimToEmpty(TB_YSDJ_Info).equals("")){
                r.setCode(-1);
                r.setMsg("今日已提交过");
                return r;
            }*/
            String time = param.get("time");
            /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            String testDate=df.format(new Date());//格式化当前日期*/
            String flag = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)=? and y.create_ss_id=? and y.STATUS=1" , String.class,time,userId);
           //先判断当前日期是否有数据
            if (!trimToEmpty(flag).equals("")){
                    r.setCode(-1);
                    r.setMsg("已有新增数据");
                    return r;
            }


            importService.tj(param);
            r.setMsg("操作成功");
        }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            throw ex;
        }finally {
            return r;
        }
    }


    //删除
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try{
        importService.del(param);
        r.setMsg("删除成功");
        }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            throw ex;
        }finally {
            return r;
        }
    }


    @PostMapping(value = "/leaveinfo")
    @ResponseBody
    public Object leaveInfo() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
//            保存中队请假信息
            importService.saveLeave(param);
            List<Map<String, Object>> list = importService.getByIdForLeave(param);
            r.setDataset(list);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
        } finally {
            return r;
        }
    }









   /* @RequestMapping(value = "/leaveinfoLeave")
    @ResponseBody
    public Object leaveinfoLeave() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            List<Map<String, Object>> list = importService.getLeave(param);
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
    }*/

}


