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
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.RyfxtjService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/ryfxtj")
public class RyfxtjAction {
    Logger logger = LogManager.getLogger(RyglAction.class.getName());

    private static String path = "admin/manage/ryfxtj";

    @Autowired
    HttpServletRequest request;

    @Autowired
    RyfxtjService ryfxtjService;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/list");
        view.addObject("userId",userId);
        return view;
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        param.put("userId", u.getUser_id());
        Page<?> page;
        try {
            page = ryfxtjService.findryfxPage(param);
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

    //添加提交页面
    @RequestMapping(value = {"/add/{id}", "/add/"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        /*Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/add");
        view.addObject("RYFXTJ", usermap);
        view.addObject("userId", userId);
        return view;*/
        ModelAndView view = new ModelAndView(path+"/add");
        if (StringUtils.isEmpty(id)) {
            Map<String, Object> m = new HashMap<>();
            UserEntity u = CustomRealm.GetLoginUser();
            String user = u.getDwmc();
            m.put("DWMC",user);
            view.addObject("RYFXTJ",m);
            return view;
        }
        /*UserEntity u = (UserEntity) subject.getPrincipal();
        String userid=u.getUser_id();*/


        try {

            Map<String, Object> m = DBHelper.queryForMap("select t.ID, t.SS_ID, t.NAME, t.SEX, t.NL,t.MZ, t.ZJ,t.SG, t.JBZL, t.GRCZS, t.JSZT,t.STZT,t.DWMC,\n" +
                    "t.SHGN,t.GRZXQK,t.CZWT,t.DCFX,to_char(t.CREATE_TIME, 'YYYY-MM-DD HH24:MI:SS') CREATE_TIME, to_char(t.UPDATE_TIME, 'YYYY-MM-DD HH24:MI:SS') UPDATE_TIME,\n" +
                    "t.CZR,t.SFTJ,(SELECT NAME  FROM BUSINESS_DICT BD  WHERE BD.TYPE_CODE = 'zw' AND BD.VALUE = t.ZJ)ZJ_NAME,（SELECT DEPTNAME NAME  FROM SS_DEPT s\n" +
                    " WHERE s.id = t.DWMC AND s.STATUS <> '-1')DWMC_NAME, t.STATUS,t.RYSTATUS from TB_RYFX t  WHERE T.ID=? ", id);
          /*  Map<String, Object> map = new HashMap<>();
            UserEntity u = CustomRealm.GetLoginUser();
            String name = u.getDeptname();
            map.put("NAME",name);
            m.put("NAME",map);*/
            view.addObject("RYFXTJ", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    @RequestMapping(value = "/save")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxsave(String dwmc, String sex, String nl, String mz, String zj, String sg, String jbzl, String grczs, String jszt, String stzt, String shgn, String grzxqk, String czwt, String dcfx,String rystatus) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
//            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            UserEntity u = CustomRealm.GetLoginUser();
            String userId =u.getUser_id();
//            param.put("ss_id",userId);
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (dwmc == null) {
                r.setCode(-1);
                r.setMsg("选择单位名称！");
                return r;
            }

            if (sex == null) {
                r.setCode(-1);
                r.setMsg("输入性别！");
                return r;
            }
            if (nl == null) {
                r.setCode(-1);
                r.setMsg("输入年龄！");
                return r;
            }
            if (mz == null) {
                r.setCode(-1);
                r.setMsg("输入名族！");
                return r;
            }
            if (zj == null) {
                r.setCode(-1);
                r.setMsg("输入职级！");
                return r;
            }
            if (sg == null) {
                r.setCode(-1);
                r.setMsg("输入身高！");
                return r;
            }
            if (jbzl == null) {
                r.setCode(-1);
                r.setMsg("输入基本资料！");
                return r;
            }
            if (grczs == null) {
                r.setCode(-1);
                r.setMsg("输入个人成长史！");
                return r;
            }
            if (jszt == null) {
                r.setCode(-1);
                r.setMsg("输入精神状态！");
                return r;
            }
            if (stzt == null) {
                r.setCode(-1);
                r.setMsg("输入身体状态！");
                return r;
            }
            if (shgn == null) {
                r.setCode(-1);
                r.setMsg("输入社会功能！");
                return r;
            }
            if (grzxqk == null) {
                r.setCode(-1);
                r.setMsg("输入个人征信情况！");
                return r;
            }
            if (czwt == null) {
                r.setCode(-1);
                r.setMsg("输入存在问题！");
                return r;
            }
            if (dcfx == null) {
                r.setCode(-1);
                r.setMsg("输入对策分析！");
                return r;
            }
//            if (sftj == null) {
//                r.setCode(-1);
//                r.setMsg("输入提交状态！");
//                return r;
//            }


            ryfxtjService.bc(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @RequestMapping(value = "/tj")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxtj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        //Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        UserEntity u = CustomRealm.GetLoginUser();
        //String userId = user.get("SS_ID").toString();
        String userId =u.getUser_id();
        param.put("userId", userId);
        Page<?> page;
        try {
            ryfxtjService.tj(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    public Map<String, Object> getUserInfo(String userId) {
        //用户信息
        Map<String, Object> usermap = DBHelper.queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,job as job_true," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE    " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.SS_ID=? ", userId);
        return usermap;
    }

    //同步上季度数据
    @RequestMapping(value = "/tb")
    @ResponseBody
    public Object tb(String ss_id) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            String userId = param.get("userId");
//            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
//            if (userEntity == null) {
//                r.setCode(-1);
//                r.setMsg("用户不存在！");
//                return r;
//            }
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("请输入用户！");
                return r;
            }
            List<Map<String, Object>> list = ryfxtjService.findusers(param);
            r.setDataset(list);
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

    //删除逻辑
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del(String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity u = CustomRealm.GetLoginUser();
            //String userId = user.get("SS_ID").toString();
            String userId =u.getUser_id();
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("请输入ID！");
                return r;
            }
            ryfxtjService.del(param);
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




}
