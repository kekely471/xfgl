package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.InvoiceService;
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

@RequestMapping(value = "/invoice")
@Controller
public class InvoiceAction {
    Logger logger = LogManager.getLogger(InvoiceAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private InvoiceService invoiceService;

    /*
     * 发票管理页面
     * */
    @RequestMapping(value = "/")
    public ModelAndView extendList() {
        ModelAndView view = new ModelAndView("admin/manage/invoice/list");
        return view;
    }

    /*
     * 发票管理页面——table表格加载接口
     * */
    @RequestMapping(value = "/tableList")
    @ResponseBody
    public Object tableList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = invoiceService.tableListLoad(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("数据加载超时！");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /*
     * 发票管理页面——获取新增、编辑页面
     * */
    @RequestMapping(value = {"/add/{isView}/{id}", "/add/{isView}"})
    public ModelAndView add(@PathVariable Boolean isView, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/invoice/add");
        view.addObject("isView", isView);
        try {
            if (StringUtils.isEmpty(id)) { //新增页面
                return view;
            } else { //编辑页面
                Map<String, Object> singleInvoice = DBHelper.queryForMap(" SELECT A.ID, A.INVOICETYPE, A.REMARK," +
                        " A.DRAWER, A.ISHISTORY, A.INVOICENAME, A.CHECKCODE, A.SHOPDEPTNAME, A.SHOPDEPTPOSITION, A.SHOPDEPTPHONE, " +
                        " A.SHOPDEPTBANKCODE, A.SELLDEPTNAME, A.SELLDEPTPOSITION, A.SELLDEPTPHONE, A.SELLDEPTBANKCODE, " +
                        " A.GOODSORSERVICENAME, A.SPECIFICATIONTYPE, A.UNTI, A.GOODSNUMBER, A.SINGLEPRICE, " +
                        " A.TAXRATE, A.PAYEE, A.REVIEWER FROM INVOICE A WHERE A.ID=? ", id);
                //扩展项值
                StringBuffer extendSql = new StringBuffer("SELECT B.FIELD_NAME,A.VALUE FROM FORM_EXTEND_VALUE A " +
                        " INNER JOIN FORM_EXTEND B " +
                        " ON A.FORM_EXTEND_ID=B.ID " +
                        " WHERE FORM_ID=? ");
                List<Map<String, Object>> extendSqlList = DBHelper.queryForList(extendSql.toString(), id);
                Map<String, Object> extendMap = new HashMap<>();
                for (int i = 0; i < extendSqlList.size(); i++) {
                    extendMap.put(extendSqlList.get(i).get("FIELD_NAME").toString(), extendSqlList.get(i).get("VALUE"));
                }
                view.addObject("INVOICE", singleInvoice);
                view.addObject("EXTEND", extendMap);
                view.addObject("id", id);
                return view;
            }
        } catch (ActionException ex) {
            throw ex;
        }
    }

    @RequestMapping(value = "/extendMap")
    @ResponseBody
    public Map<String, Object> extendMap() {
        Map<String, String> param = RequestUtils.toMap(request);
        //扩展项值
        StringBuffer extendSql = new StringBuffer("SELECT B.FIELD_NAME,A.VALUE FROM FORM_EXTEND_VALUE A " +
                " INNER JOIN FORM_EXTEND B " +
                " ON A.FORM_EXTEND_ID=B.ID " +
                " WHERE FORM_ID=? ");
        List<Map<String, Object>> extendSqlList = DBHelper.queryForList(extendSql.toString(), param.get("id"));
        Map<String, Object> extendMap = new HashMap<>();
        for (int i = 0; i < extendSqlList.size(); i++) {
            extendMap.put(extendSqlList.get(i).get("FIELD_NAME").toString(), extendSqlList.get(i).get("VALUE"));
        }
        return extendMap;
    }


    /*
     * 发票管理页面——组件渲染数据接口（公共方法可抽取到common接口类中，传参：表单唯一编码）
     * */
    @RequestMapping(value = "/renderingList")
    @ResponseBody
    public ResultEntity renderingList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            List<Map<String, Object>> renderingList = invoiceService.getRenderingKey(param);
            r.setDataset(renderingList);
            r.setMsg("组件渲染数据加载成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("组件渲染数据加载失败");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /*
     * 发票管理页面——新增、编辑
     * */
    @RequestMapping(value = "/addOrUpdate")
    @ResponseBody
    public Object addOrUpdate() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            invoiceService.addOrUpdate(param);
            r.setMsg("操作成功！");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("操作失败！");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /*
     * 发票管理页面——删除（假删除）
     * */
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            invoiceService.del(param);
            r.setMsg("删除成功！");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("删除失败！");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /*
     * 发票管理页面——彻底删除（真删除）
     * */
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            invoiceService.realDel(param);
            r.setMsg("彻底删除成功！");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("彻底删除失败！");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }
}
