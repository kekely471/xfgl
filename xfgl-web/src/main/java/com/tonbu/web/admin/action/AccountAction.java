package com.tonbu.web.admin.action;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.service.AccountService;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.CryptUtils;
import com.tonbu.web.admin.service.AccService;
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
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/account")
@Controller
public class AccountAction {

    Logger logger = LogManager.getLogger(AccountAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccService accService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/account/list");
        return view;
    }


    @RequestMapping(value = "/editPwd")
    public ModelAndView editpwd() {
        ModelAndView view = new ModelAndView("editpwd");
        return view;
    }

    @RequestMapping(value = "/relUser/{id}")
    public ModelAndView relUser(@PathVariable String id) {
        ModelAndView view = new ModelAndView("admin/manage/account/relUser");
        view.addObject("id", id);
        return view;
    }


    @ResponseBody
    @RequestMapping("/updatePwd")
    public Object updatePassword(String oldpassword, String password, String password2) {
        return accService.updatePassword(oldpassword, password, password2);
    }

    @ResponseBody
    @RequestMapping("/initPwd")
    public Object initPassword() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            String ids = param.get("ids");

            String[] id_s = ids.split("\\+");
            for (int i = 0; i < id_s.length; i++) {

                String salt = CryptUtils.getRandomSalt(32);
                String npwd = CryptUtils.md5(Constants.INIT_PASSWORD, salt);
                DBHelper.update("UPDATE ACCOUNT SET PASSWORD=?,SALT=? WHERE ID=? ", npwd, salt, id_s[i]);
            }
            r.setMsg(Constants.INIT_PASSWORD);
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
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
            page = accountService.loadList(param);
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
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/account/add");
        if (StringUtils.isEmpty(id)) {
            view.addObject("STATUS", 1);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT A.ID,A.LOGINNAME,A.WX_NO,A.WX_ACCOUNT,A.STATUS,A.REMARK," +
                    "(SELECT WM_CONCAT(TO_CHAR(R.ID)) FROM  ACCOUNT_ROLE AR LEFT JOIN SS_ROLE R ON R.ID=AR.ROLE_ID WHERE AR.ACCOUNT_ID=A.ID) AS ROLEID  " +
                    "FROM ACCOUNT A WHERE A.ID=? ", id);
            //String sort=DBHelper.queryForScalar("SELECT MAX(SORT) FROM ACCOUNT ",String.class);
            //iew.addObject("sort",sort);
            view.addObject("ACCOUNT", m);
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
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT A.ID,A.WX_NO,A.WX_ACCOUNT,A.STATUS,A.REMARK FROM ACCOUNT A WHERE A.ID=? ", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
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
            accountService.save(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
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
            accountService.del(param);
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
            accountService.realDel(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @RequestMapping(value = "/delBind")
    @ResponseBody
    public Object delBind() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            accountService.delBind(param);
            r.setMsg("解绑成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @RequestMapping(value = "/getBindUser")
    @ResponseBody
    public Object getBindUser() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = accountService.getBindUser();
            r.setDataset(l);
            r.setMsg("获取数据成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @RequestMapping(value = "/getBind")
    @ResponseBody
    public Object getBind() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = accountService.getBind();
            r.setDataset(l);
            r.setMsg("获取数据成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @RequestMapping(value = "/bind")
    @ResponseBody
    public Object bing() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            accountService.bind(param);
            r.setMsg("绑定用户成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 获取基本信息页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/basicInfoByPerson")
    public ModelAndView basicInfoByPerson() {
        ModelAndView view = new ModelAndView("admin/manage/user/add");
        UserEntity u = CustomRealm.GetLoginUser();
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.USER_TYPE,V.USERNAME,V.STATUS,V.SORT,V.SEX,V.REMARK,V.OFFICETEL,V.MOBILE,V.EMAIL,V.DEPT_ID,V.FILE_ID,V.NICK_NAME,V.AVATAR,V.SIGN FROM SS_USER V WHERE V.ID=? ", u.getId());
            view.addObject("USER", m);
            view.addObject("id", u.getId());
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }
}
