package com.tonbu.web.admin.action;

import com.tonbu.web.admin.service.LogService;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping(value = "/logs")
@Controller
public class LogAction {

    Logger logger = LogManager.getLogger(LogAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private LogService logService;


    @RequestMapping(value = "/login/")
    public ModelAndView loginLogsHome() {
        ModelAndView view = new ModelAndView("admin/manage/logs/login/list");
        return view;
    }

    @RequestMapping(value = "/op/")
    public ModelAndView opLogsHome() {
        ModelAndView view = new ModelAndView("admin/manage/logs/op/list");
        return view;
    }


    @RequestMapping(value = "/login/list")
    @ResponseBody
    public Object loginList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = logService.loadLoginLogs(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        finally {
            return r;
        }
    }


    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/login/realDel")
    @ResponseBody
    public Object relDelLogin() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            logService.realDelLogin(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        finally {
            return r;
        }
    }



    @RequestMapping(value = "/op/list")
    @ResponseBody
    public Object opList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = logService.loadOpLogs(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        finally {
            return r;
        }
    }

    /**
     * 获取一条数据
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/op/get/{id}")
    @ResponseBody
    public ModelAndView get(@PathVariable String id) {

        ModelAndView view = new ModelAndView("admin/manage/logs/op/detail");
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.USERNAME,V.USER_IP,V.USER_IP,V.USER_APP,V.URL,V.PARAM,V.OPERATION_TYPE,V.OPERATION_TIME,V.MODEL_NAME,V.LOGINNAME,V.LOCATION,V.CONTENT_BACK,V.ACCOUNT_ID " +
                    "FROM SS_LOG_OPERATE V WHERE V.ID=? ", id);
            view.addObject("LOG_OP", m);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/op/realDel")
    @ResponseBody
    public Object relDelOp() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            logService.realDelOp(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        finally {
            return r;
        }
    }

    @RequestMapping(value = "/business/")
    public ModelAndView businessLogsHome() {
        ModelAndView view = new ModelAndView("wjitsm/logs/list");
        return view;
    }

    @RequestMapping(value = "/business/list")
    @ResponseBody
    public Object businessList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = logService.loadBusinessLogs(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        finally {
            return r;
        }
    }

}
