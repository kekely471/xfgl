package com.tonbu.web.admin.action;


import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.KqpzService;
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
import java.util.Map;

@RequestMapping(value = "/kqpz")
@Controller
public class KqpzAction {
    Logger logger = LogManager.getLogger(KqpzAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private KqpzService kqpzService;

    @RequestMapping(value = "/")
    public ModelAndView jkhome() {
        ModelAndView view = new ModelAndView("admin/manage/kqpz/list");
        return view;
    }

    @RequestMapping(value = {"/add/{id}", "/add"})
    public ModelAndView jkadd(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/kqpz/add");
        try {
            if (StringUtils.isEmpty(id)) {
                return view;
            }
            Map<String, Object> m = DBHelper.queryForMap(" select * from tb_kqpz where id= ? ", id);
            String sbsj = m.get("sbsj").toString();
            String xbsj = m.get("xbsj").toString();
            String[] sbsjs = sbsj.split("-");
            String[] xbsjs = xbsj.split("-");
            sbsjs[0].replace(" ", "");
            sbsjs[1].replace(" ", "");
            xbsjs[0].replace(" ", "");
            xbsjs[1].replace(" ", "");
            Map<String,Object> map = new HashMap<>();
            map.put("SBKSSJ",sbsjs[0]);
            map.put("SBJSSJ",sbsjs[1]);
            map.put("XBKSSJ",xbsjs[0]);
            map.put("XBJSSJ",xbsjs[1]);
            view.addObject("KQPZ", m);
            view.addObject("id", id);
            view.addObject("SJ", map);
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
            page =kqpzService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }
    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            param.put("ip", request.getRemoteAddr());
            kqpzService.save(param);
            r.setMsg("操作成功");
        }catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object relDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            kqpzService.realDel(param);
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
