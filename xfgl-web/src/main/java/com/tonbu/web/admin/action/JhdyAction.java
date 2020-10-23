package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.helper.JwtTokenHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.JhdyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/jhdy")
@Controller
public class JhdyAction {
    Logger logger = LogManager.getLogger(JhdyAction.class.getName());

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    HttpServletRequest request;

    @Autowired
    JhdyService jhdyService;

    //问题查看入口
    @RequestMapping(value = "/wtck/")
    public ModelAndView wtck(){
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = jwtTokenHelper.generateToken(userEntity.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView("admin/manage/jhdy/list");
        view.addObject("userId",userId);
        return view;
    }

    //问题查看
    @RequestMapping(value = "/wtck/list")
    @ResponseBody
    public Object wtckList(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = userEntity.getUser_id();
        String dwmc = userEntity.getDwmc();
        String job = null==userEntity.getJob() ? "":userEntity.getJob();
        //String job =null==user.get("JOB") ? "": user.get("JOB").toString();
        param.put("job",job);
        param.put("dwmc",dwmc);
        param.put("userId",userId);
        Page<?> page;
        try {
            page = jhdyService.wtckList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setMsg("网络异常");
            r.setCode(-1);
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //问题管理新增编辑
    @RequestMapping(value = "/add/{id}")
    public ModelAndView wtckadd(@PathVariable(required = false)String id){
        ModelAndView view = new ModelAndView();
        Map<String, Object> map = DBHelper.queryForMap("select a.ss_id,b.DWMC, a.id,c.DEPTNAME,b.NAME as FBR,u1.name as CLR, " +
                " a.TITLE, " +
                " a.DESCRIBE, " +
                " a.status, " +
                " a.SFGk, " +
                " d.FXDESCRIBE, " +
                " TO_CHAR( a.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME " +
                " from  TB_JHDYWT a " +
                " LEFT JOIN TB_USER b on b.ss_id = a.ss_id " +
                " LEFT JOIN ss_dept c on c.id = b.DWMC " +
                " LEFT JOIN TB_JHDYFX d on d.wt_id = a.id " +
                " left join tb_user u1 on u1.ss_id = d.clr_id where a.id=?",id);
        view.setViewName("admin/manage/jhdy/add");
        view.addObject("id",id);
        view.addObject("WTCK",map);
        return view;
    }


    //问题管理入口
    @RequestMapping(value = "/wtgl/")
    public ModelAndView wtgl(){
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = jwtTokenHelper.generateToken(userEntity.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView("admin/manage/jhdy/wtgl/list");
        view.addObject("userId",userId);
        return view;
    }

    //问题管理
    @RequestMapping(value = "/wtgl/list")
    @ResponseBody
    public Object wtglList(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = userEntity.getUser_id();
        param.put("userId",userId);
        Page<?> page;
        try {
            page = jhdyService.wtglList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setMsg("网络异常");
            r.setCode(-1);
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //问题管理新增编辑
    @RequestMapping(value = {"/wtgl/add/{id}","/wtgl/add"})
    public ModelAndView wtgladd(@PathVariable(required = false)String id){
        ModelAndView view = new ModelAndView();
        if (StringUtils.isBlank(id)){
            UserEntity userEntity = CustomRealm.GetLoginUser();
            Map<String, Object> map = DBHelper.queryForMap("SELECT NAME FROM TB_USER WHERE SS_ID = ?", userEntity.getUser_id());
            view.addObject("WTGL",map);
            view.addObject("id",id);
            view.setViewName("admin/manage/jhdy/wtgl/add");
            return view;
        }
        Map<String, Object> map = DBHelper.queryForMap("SELECT t.ID,t.TITLE,t.DESCRIBE,u.name,t.STATUS,t.SFGK,TO_CHAR(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,TO_CHAR( t.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME\n" +
                "FROM TB_JHDYWT t LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID WHERE t.ID=?",id);
        view.setViewName("admin/manage/jhdy/wtgl/add");
        view.addObject("id",id);
        view.addObject("WTGL",map);
        return view;
    }


    //问题管理保存
    @RequestMapping(value = "/wtgl/save")
    @ResponseBody
    public Object wtglsave() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser();
            String userId = userEntity.getUser_id();
            param.put("userId",userId);
            jhdyService.wtglsave(param);
            r.setMsg("保存成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //问题管理删除
    @RequestMapping(value = "/wtgl/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            jhdyService.wtgldel(param);
            r.setMsg("删除成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    //问题反馈入口
    @RequestMapping(value = "/wtfx/")
    public ModelAndView wtfk(){
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String userId = jwtTokenHelper.generateToken(userEntity.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView("admin/manage/jhdy/wtfx/list");
        view.addObject("userId",userId);
        return view;
    }

    //问题管理
    @RequestMapping(value = "/wtfx/list")
    @ResponseBody
    public Object wtfxList(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = jhdyService.wtfxList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setMsg("网络异常");
            r.setCode(-1);
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }



    //问题管理新增编辑
    @RequestMapping(value = "/wtfx/add/{id}")
    public ModelAndView wtfxadd(@PathVariable(required = false)String id){
        ModelAndView view = new ModelAndView();
        Map<String, Object> map = DBHelper.queryForMap("select a.ss_id,b.DWMC, a.id,c.DEPTNAME,b.NAME as FBR,u1.name as CLR, " +
                " a.TITLE, " +
                " a.DESCRIBE, " +
                " a.status, " +
                " a.SFGk, " +
                " d.FXDESCRIBE, " +
                " TO_CHAR( a.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME " +
                " from  TB_JHDYWT a " +
                " LEFT JOIN TB_USER b on b.ss_id = a.ss_id " +
                " LEFT JOIN ss_dept c on c.id = b.DWMC " +
                " LEFT JOIN TB_JHDYFX d on d.wt_id = a.id " +
                " left join tb_user u1 on u1.ss_id = d.clr_id where a.id=?",id);
        view.setViewName("admin/manage/jhdy/wtfx/add");
        view.addObject("id",id);
        view.addObject("WTFX",map);
        return view;
    }

    //问题分析保存
    @RequestMapping(value = "/wtfx/save")
    @ResponseBody
    public Object wtfxsave() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser();
            String userId = userEntity.getUser_id();
            param.put("userId",userId);
            jhdyService.wtfxsave(param);
            r.setMsg("保存成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }











}
