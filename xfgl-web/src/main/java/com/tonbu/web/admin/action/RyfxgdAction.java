package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.RyfxgdService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.tonbu.support.helper.JwtTokenHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/ryfxgd")
public class RyfxgdAction {
    Logger logger = LogManager.getLogger(RyglAction.class.getName());

    private static String path = "admin/manage/ryfxgd";

    @Autowired
    HttpServletRequest request;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    RyfxgdService ryfxgdService;

    @RequestMapping(value = "/")
    public ModelAndView home(){
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/list");
        view.addObject("userId",userId);
        return view;
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(){
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        param.put("userId",u.getUser_id());
//        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
//        String userId = user.get("SS_ID").toString();
//        param.put("userId", userId);
        Page<?> page;
        try{
          page=ryfxgdService.findrygdPage(param);
            r.setDataset(page);
            r.setMsg("");
        }catch(RuntimeException ex){
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        }finally {
            return r;
        }
    }

    @RequestMapping(value = "/add/{id}")
    @ResponseBody
    public ModelAndView rygdadd(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView(path+"/add");
        try {
            Map<String, Object> m = DBHelper.queryForMap("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc) dwmc_name,t.dwmc,t.name,(select name from  business_dict b WHERE  b.type_code='sex' AND b.value=t.sex)sexname,sex,\n" +
                    "  nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "  TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME,status,czr,sftj,\n" +
                    "  case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' end STATUSNAME,\n" +
                    "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME,t.RYSTATUS\n" +
                    "   from TB_RYFX t left join TB_USER c on c.SS_ID = t.SS_ID where t.id=?", id);
            view.addObject("RYFXGD", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    @RequestMapping(value = "/gd")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxgd() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            ryfxgdService.ryfxgd(param);
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



}
