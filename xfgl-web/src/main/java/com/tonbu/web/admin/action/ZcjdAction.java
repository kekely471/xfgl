package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.ZcjdService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/zcjd")
public class ZcjdAction {

    Logger logger = LogManager.getLogger(ZcjdAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    ZcjdService zcjdService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/zcjd/list");
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
            page = zcjdService.loadList(param);
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
        ModelAndView view = new ModelAndView("admin/manage/zcjd/add");
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
            zcjdService.save(param);
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
            zcjdService.updateStatus(param);
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
            zcjdService.del(param);
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
            Page<?> page = zcjdService.getPageInformationList(type, current, rowcount);
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
            Map<String, Object> map = zcjdService.getPageInformationById(ID);
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


}
