package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.FormFrameService;
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

@RequestMapping(value = "/formFrame")
@Controller
public class FormFrameAction {

    Logger logger = LogManager.getLogger(FormFrameAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private FormFrameService formService;

    /*
     * 表单项管理页面
     * */
    @RequestMapping(value = "/")
    public ModelAndView extendList() {
        ModelAndView view = new ModelAndView("admin/manage/formFrame/list");
        return view;
    }

    /*
     * 表单项管理页面——table表格加载接口
     * */
    @RequestMapping(value = "/extendTableList")
    @ResponseBody
    public Object extendTableList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            List list = formService.extendTableListLoad(param);
            r.setDataset(list);
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

    @RequestMapping(value = "/newExtendTableList")
    @ResponseBody
    public Object newExtendTableList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = formService.newExtendTableListLoad(param);
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
     * 表单项管理页面——获取新增、编辑页面
     * */
    @RequestMapping(value = {"/extendAdd/{isView}/{id}", "/extendAdd/{isView}"})
    public ModelAndView extendAdd(@PathVariable Boolean isView, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/formFrame/add");
        view.addObject("isView", isView);
        try {
            if (StringUtils.isEmpty(id)) { //新增页面
                Integer sort = DBHelper.queryForScalar(" SELECT (MAX(SORT)+1) FROM SS_FORMFRAME WHERE FORM_TYPE=(SELECT FORM_TYPE FROM SS_FORMFRAME WHERE ID=?) ", Integer.class, id);
                Map<String, Object> m = new HashMap<>();
                m.put("SORT", sort);
                view.addObject("FormComponent", m);
                return view;
            }
            Map<String, Object> singleFormComponent = DBHelper.queryForMap(" SELECT A.ID, A.LABLE_NAME, A.FIELD_NAME,A.DEFAULT_VALUE, A.SORT, A.MAX_LENGTH, A.MIN_LENGTH, " +
                    " A.COMPONENT_TYPE, A.ISDISABLED, A.FORM_TYPE, A.VALIDATE_RULES, " +
                    " A.COMPONENT_WIDTH, A.COMPONENT_LAYOUT, A.COMPONENT_URL, A.COMPONENT_URL_ATTR, " +
                    " A.SELECTCOMPONENTVALUE, A.SELECTCOMPONENTNAME, A.UPLOADID, A.UPLOADPOSITION,A.DATATYPE as \"dataType\",A.TYPE_CODE AS \"type_code\", " +
                    " A.ISMULTICHOOSE, A.ATTACHMENTTYPE, A.DATETYPE, A.DATEFORMAT, A.TREETITLE, A.IDKEY, A.PIDKEY, A.NAMEKEY,A.SEARCH_URL,A.ISMULTIPLE FROM SS_FORMFRAME A WHERE ID=? ", id);
            view.addObject("FormComponent", singleFormComponent);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /*
     * 表单项管理页面——新增、编辑
     * */
    @RequestMapping(value = "/extendAddOrUpdate")
    @ResponseBody
    public Object extendAddOrUpdate() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            if (StringUtils.isNotBlank(param.get("dataType")) && param.get("dataType").equals("2") && StringUtils.isNotBlank(param.get("componentUrlAttr"))) {
                String table_name = param.get("componentUrlAttr").replace("'", "");
                if (table_name.indexOf("table_name") == -1) {
                    r.setCode(-1);
                    r.setMsg("表名不符合规则，请输入键值对格式table_name:ss_user");
                } else {
                    table_name = table_name.substring(table_name.indexOf(":") + 1);
                    int count = formService.checkTableNameIsExist(table_name);
                    if (count <= 0) {
                        r.setCode(-1);
                        r.setMsg("数据库表不存在，请重新输入");
                    }
                }

            }
            if (r.getCode() == 1) {
                formService.extendAddOrUpdate(param);
                r.setMsg("操作成功！");
            }
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
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
     * 表单项管理页面——删除（假删除）
     * */
    @RequestMapping(value = "/extendDel")
    @ResponseBody
    public Object extendDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            formService.extendDel(param);
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
     * 表单项管理页面——彻底删除（真删除）
     * */
    @RequestMapping(value = "/extendRealDel")
    @ResponseBody
    public Object extendRealDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            formService.extendRealDel(param);
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
