package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.CustomizeException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.DESUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


@Controller
public class LoginAction {

    Logger logger = LogManager.getLogger(LoginAction.class.getName());

    @Value("${im.ip}")
    private String ip;

    @RequestMapping(value = "/login")
    public String login() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            return "redirect:main";
        } else {
            return "login";
        }
    }

    /**
     * 移动端
     * @return
     */
    @RequestMapping(value = "/mobile/login")
    public String mobileLogin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            return "redirect:mobile";
        } else {
            return "mobileLogin";
        }
    }

    @RequestMapping(value = "/index")
    public String index() {

        return "index";
    }


    @RequestMapping(value = "/home")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("home");
        view.addObject("attr", "12345");
        return view;
    }


    @RequestMapping(value = "/main")
    public String main() {
        return "main";
    }


    /**
     * 登录验证 客户端
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ResponseBody
    public Object check(String username, String password) {

        ResultEntity r = new ResultEntity();
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(
                username, password);
        usernamePasswordToken.setRememberMe(true);
        String msg = "";
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(usernamePasswordToken); // 完成登录
            UserEntity u = (UserEntity) subject.getPrincipal();
            Session session = subject.getSession();
            logger.info("sessionId:" + session.getId());
            logger.info("sessionTimeOut:" + session.getTimeout());
            logger.info("sessionHost:" + session.getHost());
            subject.getSession().setAttribute("loginuser", u);
            subject.getSession().setAttribute("im_ip", ip);
            subject.getSession().setAttribute("userToken", DESUtils.encrypt(u.getUser_id()));
            subject.getSession().setAttribute("ip", ip);
            Collection<Object> lll = session.getAttributeKeys();
            msg = "登陆成功！";
            loginLog(u.getUser_id(), u.getLoginname(), u.getUsername(), 0, 1, session.getHost());
            //更改登录时间
            DBHelper.execute("UPDATE ACCOUNT SET LAST_LOGIN_TIME = SYSDATE WHERE ID = ?",u.getId());
            List<Object> l = new ArrayList<Object>();
            l.add(u);
            r.setCode(1);
            r.setDataset(l);
        } catch (Exception exception) {
            if (exception instanceof UnknownAccountException) {
                logger.error("账号不存在： -- > UnknownAccountException");
                msg = "登录失败，账号不存在或未关联系统用户！";
                throw new CustomizeException(msg);
            } else if (exception instanceof IncorrectCredentialsException) {
                logger.error(" 密码不正确： -- >IncorrectCredentialsException");
                msg = "登录失败，密码不正确！";
                throw new CustomizeException(msg);
            } else {
                logger.error("else -- >" + exception);
                msg = "登录失败，发生未知错误：" + exception;
                throw new CustomizeException("登录失败，发生未知错误");
            }
        }
        r.setMsg(msg);
        return r;
    }


    /**
     * 权限获取
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/auth/list", method = RequestMethod.GET)
    @ResponseBody
    public Object check(String id) {
        ResultEntity r = new ResultEntity();
        String msg = "";
        if (!StringUtils.isNotEmpty(id)) {
            r.setCode(-1);
            r.setMsg("父编号不能为空");
            return r;
        }
        try {
            //获取权限列表
            UserEntity u = CustomRealm.GetLoginUser();
            //重新set菜单。
//            List<Map<String, Object>> newMenuList = DBHelper.queryForList("select sm.ID,sm.MENUNAME,sm.MENUTYPE,sm.PARENTID,(SELECT m.MENUNAME FROM SS_menu m where m.id = sm.parentid)as PMENUNAME,sm.SYMBOL,sm.ICON,sm.URL,sm.SORT,sm.STATUS from ss_menu sm where id <>00 and status=1 order by sm.Sort,sm.id ");
//            u.setMenus(newMenuList);
            List<Object> l = getMenus(id, u.getMenus());
            r.setCode(1);
            r.setDataset(l);
        } catch (Exception exception) {
            logger.error("权限获取异常：" + exception);
            msg = "权限获取异常：" + exception;
            r.setCode(-1);
        } finally {
            r.setMsg(msg);
            return r;
        }
    }

    private List<Object> getMenus(String id, List<Map<String, Object>> menus) {
        List<Object> r = new ArrayList<>();
        for (Map<String, Object> map : menus) {
            if (map.get("ID").toString().equals("0005")){//去掉了考试管理部分，因为考试管理部分代码并不在项目中
                continue;
            }
            if (StringUtils.equals(id, map.get("PARENTID").toString()) && map.get("MENUTYPE").toString().equals("0")) {
                Map<String, Object> map_c = new HashMap<>();
                map_c.put("id", map.get("ID"));
                map_c.put("pid", map.get("PARENTID"));
                map_c.put("url", map.get("URL"));
                map_c.put("icon", map.get("ICON"));
                map_c.put("title", map.get("MENUNAME"));
                //找兒子，看兒子是不是有數據，如果有，遞歸；
                List<Object> c = getMenus(map.get("ID").toString(), menus);
                if (c.size() > 0) {
                    map_c.put("children", c);

                }
                r.add(map_c);
            }
        }
        return r;
    }


    /**
     * 注销退出
     **/
    @RequestMapping(value = "/logout")
    public Object logout() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Session session = subject.getSession();
        loginLog(u.getId(), u.getLoginname(), u.getUsername(), 1, 1, session.getHost());
        logger.info("***用户登出：" + u.getLoginname());
        subject.logout();
        return "redirect:login";
    }

    /**
     * 未授权
     **/
    @RequestMapping(value = "/unauth")
    public Object unauth() {
        logger.error("未授权");
        return "redirect:login";
    }


    /**
     * 写登录登出记录
     *
     * @param user_id
     * @param index   0:登录(客户端) 1:下班 2：注销 3:登录管理端 4:登出管理端
     */
    public void loginLog(String user_id, String loginname, String username, int index, int loginstatus, String host) {
        try {
            DBHelper.execute("INSERT INTO SS_LOG_LOGIN (ID,USER_ID,LOGINNAME,USERNAME,LOGINTIME,TYPE,LOGINIP,REMARK,STATUS) VALUES (SEQ_SS_LOG.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                    user_id, loginname, username, model_name[index], host, "", loginstatus);
        } catch (Exception exception) {
            logger.error("登录日志写入异常：" + exception);
        }
    }

    private static String[] model_name = {"登录", "登出"};
}




