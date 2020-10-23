package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.helper.JwtTokenHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.BgtService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/bgt")
public class BgtAction {

    Logger logger = LogManager.getLogger(BgtAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    BgtService bgtService;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/bgt/list");
        return view;
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = bgtService.loadList(param);
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
     * s
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add/"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/bgt/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("select id," +
                    "       TITLE," +
                    "       DESCRIBE," +
                    "       URL," +
                    "       TYPE," +
                    "       VIEWED," +
                    "       to_char(CREATE_DATE, 'YYYY-MM-DD HH24:MI:SS') CREATE_DATE," +
                    "       CREATE_SS_ID," +
                    "       TUPIAN," +
                    "       to_char(UPDATE_TIME, 'YYYY-MM-DD HH24:MI:SS') UPDATE_TIME," +
                    "              (SELECT NAME" +
                    "          FROM BUSINESS_DICT BD" +
                    "         WHERE BD.TYPE_CODE = 'information_type'" +
                    "           AND BD.VALUE = t.TYPE) TYPE_NAME," +
                    "       type as type_true," +
                    "       STATUS," +
                    "       DETAILS," +
                    "       FILE_ID," +
                    "       EXTERNALLY" +
                    "  from TB_INFORMATION t" +
                    " where t.id = ?", id);
            view.addObject("XXFB", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /**
     * 保存数据
     *
     * @return ModelAndView
     */

    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            bgtService.save(param);
            r.setMsg("操作成功");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
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
     * 状态修改
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/updatestatus")
    @ResponseBody
    public Object updateStatus() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            bgtService.updateStatus(param);
            r.setMsg("修改成功");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
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
     * 删除数据，jia删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            bgtService.del(param);
            r.setMsg("删除成功");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    //信息列表
    @RequestMapping(value = "/getpageinformationlist")
    @ResponseBody
    public Object getPageInformationList() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));

            String type = trimToEmpty(param.get("TYPE"));

            //分页查询
            Page<?> page = bgtService.getPageInformationList(type, current, rowcount);
            r.setDataset(page);
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

    //信息详情
    @RequestMapping(value = "/getpageinformationbyid")
    @ResponseBody
    public Object getPageInformationById() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            String ID = trimToEmpty(param.get("ID"));
            Map<String, Object> map = bgtService.getPageInformationById(ID);
            results.add(map);
            r.setDataset(results);
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

    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }


    @RequestMapping(value = "/wtfb/")
    public ModelAndView wtfb(){
        ModelAndView view = new ModelAndView("admin/manage/bgt/wtfb/list");
        return view;
    }


    //问题发布
    @RequestMapping(value = "/wtfb/list")
    @ResponseBody
    public Object wtfblist(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = bgtService.loadList(param);
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


    @RequestMapping(value = "/wtsb/")
    public ModelAndView wtsb(){
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = jwtTokenHelper.generateToken(userEntity.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView("admin/manage/bgt/wtsb/list");
        view.addObject("userId",userId);
        return view;
    }


    //问题上报
    @RequestMapping(value = "/wtsb/list")
    @ResponseBody
    public Object wtsblist(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        UserEntity userEntity = CustomRealm.GetLoginUser();
        param.put("userId",userEntity.getUser_id());
        Page<?> page;
        try {
            page = bgtService.wtsbList(param);
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
    问题上报的新增和编辑
     */
    @RequestMapping(value = {"/wtsb/add/{id}","/wtsb/add/"})
    public ModelAndView wtsbadd(@PathVariable(required = false) String id){


        ModelAndView view = new ModelAndView("admin/manage/bgt/wtsb/add");
        if (StringUtils.isEmpty(id)){
            UserEntity userEntity = CustomRealm.GetLoginUser();
            Map<String, Object> map = DBHelper.queryForMap("SELECT NAME  FROM tb_user WHERE SS_ID = ?", userEntity.getUser_id());
            view.addObject("SS_ID",userEntity.getUser_id());
            view.addObject("WTSB",map);
            view.addObject("id",id);
            return view;

        }

        try {
            Map<String, Object> map = DBHelper.queryForMap("select t.ID,t.SS_ID,t.TITLE,t.DESCRIBE,u.NAME,t.STATUS,to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME\n"+
                    "from TB_BGTWTSB t left join tb_user u on u.ss_id = t.ss_id where t.ID=?",id);
            view.addObject("WTSB", map);
            view.addObject("id",id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }

    }

    //保存
    @RequestMapping(value = "/wtsb/save")
    @ResponseBody
    public Object wtsbsave(){
        ResultEntity r = new ResultEntity(1);
        try{
            //整合参数
            Map<String,String> param= RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser();
            String userId = userEntity.getUser_id();
            param.put("userId",userId);
            bgtService.bc(param);
            r.setMsg("操作成功");
        }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw  ex;
        }finally {
            return r;
        }
    }

    //问题上报的删除
    @RequestMapping(value = "/wtsb/del")
    @ResponseBody
    public Object wtsbdel(){
        ResultEntity r =new ResultEntity(1);
        try{
            Map<String, String> param = RequestUtils.toMap(request);
            bgtService.wtsb(param);
            r.setMsg("删除成功");
        }catch (RuntimeException ex){
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            throw  ex;
        }finally {
            return r;
        }
    }







}
