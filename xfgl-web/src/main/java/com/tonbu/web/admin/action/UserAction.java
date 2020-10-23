package com.tonbu.web.admin.action;

import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.admin.service.UserService;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/user")
public class UserAction {

    Logger logger = LogManager.getLogger(UserAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/user/list");
        return view;
    }


    @RequestMapping(value = "/account/{id}")
    public ModelAndView account(@PathVariable String id) {
        ModelAndView view = new ModelAndView("admin/manage/user/account");
        return view;
    }

    /**
     * 获取基础信息页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/basicInfo/{id}")
    public ModelAndView info(@PathVariable String id) {
        ModelAndView view = new ModelAndView("admin/manage/user/basicInfo");
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.USER_TYPE,V.USERNAME,V.STATUS,V.SORT,V.SEX,V.REMARK,V.OFFICETEL,V.MOBILE,V.EMAIL,V.DEPT_ID FROM SS_USER V WHERE V.ID=? ", id);
            view.addObject("USER", m);
            view.addObject("id",id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = userService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{selectId}/{id}", "/add/{selectId}"})
    public ModelAndView add(@PathVariable String selectId,  @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/user/add");
        try {
            if (StringUtils.isEmpty(id)) {
                Integer sort = DBHelper.queryForScalar("SELECT (MAX(SORT)+1) FROM SS_USER ", Integer.class);
                Map<String, Object> m = new HashMap<>();
                m.put("SORT", sort);
                view.addObject("USER", m);
                view.addObject("treeId", selectId);
                return view;
            }

            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.USER_TYPE,V.USERNAME,V.STATUS,V.SORT,V.SEX,V.REMARK,V.OFFICETEL,V.MOBILE,V.EMAIL,V.DEPT_ID,V.NICK_NAME,V.AVATAR,V.SIGN FROM SS_USER V WHERE V.ID=? ", id);
            //String sort=DBHelper.queryForScalar("SELECT MAX(SORT) FROM ACCOUNT ",String.class);
            //iew.addObject("sort",sort);
            view.addObject("USER", m);
            view.addObject("treeId", '0');
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }


    /**
     * 获取一条数据
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/get/{id}")
    @ResponseBody
    public Object get(@PathVariable String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.USER_TYPE,V.USERNAME,V.STATUS,V.SORT,V.SEX,V.REMARK,V.OFFICETEL,V.MOBILE,V.EMAIL,V.DEPT_ID FROM SS_USER V WHERE V.ID=?", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
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
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            userService.save(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /**
     * 删除数据，假删除
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
            userService.del(param);
            r.setMsg("删除成功");
        }catch (JSONException e){
            r.setCode(-1);
            r.setMsg(e.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object relDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            userService.realDel(param);
            r.setMsg("彻底删除成功！");
        } catch (JSONException e){
            r.setCode(-1);
            r.setMsg(e.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }
}
