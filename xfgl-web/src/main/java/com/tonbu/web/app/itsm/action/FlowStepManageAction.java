package com.tonbu.web.app.itsm.action;

import com.tonbu.web.app.itsm.service.FlowManageService;
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


@RequestMapping(value = "/flow/step/manage")
@Controller
public class FlowStepManageAction {
    Logger logger = LogManager.getLogger(FlowStepManageAction.class.getName());


    @Autowired
    HttpServletRequest request;


    @Autowired
    FlowManageService flowManageService;


    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/setting/flow/step/list");
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
            page = flowManageService.loadStepList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
    public ModelAndView add(@PathVariable String selectId, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/setting/flow/step/add");
        if (StringUtils.isEmpty(id)) {
            String sort = DBHelper.queryForScalar("SELECT (MAX(SORT)+1) FROM FLOW_STEP ", String.class);
            Map<String, Object> m = new HashMap<>();
            m.put("SORT", sort);
            view.addObject("STEP", m);
            view.addObject("treeId", selectId);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.STEP_NAME,V.STEP_TYPE,V.SORT,V.STATUS,V.REMARK,V.PRE_STEP_ID,V.FLOW_NO " +
                    "FROM FLOW_STEP V WHERE V.ID=? ", id);

            String flow_id = DBHelper.queryForScalar("SELECT V.ID FROM FLOW_INFO V WHERE V.FLOW_NO=? ", String.class, m.get("FLOW_NO"));
            m.put("FLOW_ID", flow_id);
            view.addObject("STEP", m);
            view.addObject("treeId", "0");
            view.addObject("id",id);
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
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.STEP_NAME,V.STEP_TYPE,V.SORT,V.STATUS,V.REMARK,V.PRE_STEP_ID,V.FLOW_NO " +
                    "FROM FLOW_STEP V WHERE V.ID=? ", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
            flowManageService.saveStep(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
            flowManageService.delStep(param);
            r.setMsg("删除成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
            flowManageService.realDelStep(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            return r;
        }
    }


}
