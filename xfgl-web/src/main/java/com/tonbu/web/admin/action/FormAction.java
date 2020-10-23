package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.FormService;
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

@RequestMapping(value = "/form")
@Controller
public class FormAction {

    Logger logger = LogManager.getLogger(FormAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private FormService formService;

    /*
     * 表单项管理页面
     * */
    @RequestMapping(value = "/extendList")
    public ModelAndView extendList() {
        ModelAndView view = new ModelAndView("admin/manage/form/extendList");
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
        Page<?> page;
        try {
            page = formService.extendTableListLoad(param);
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
    @RequestMapping(value = {"/extendAdd/{selectId}/{isView}/{id}", "/extendAdd/{selectId}/{isView}"})
    public ModelAndView extendAdd(@PathVariable String selectId, @PathVariable Boolean isView, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/form/extendAdd");
        view.addObject("isView", isView);
        try {
            if (StringUtils.isEmpty(id)) { //新增页面
                Integer sort = DBHelper.queryForScalar(" SELECT (MAX(SORT)+1) FROM FORM_EXTEND WHERE FORM_TYPE=? ", Integer.class, selectId);
                Map<String, Object> m = new HashMap<>();
                if (sort == null && !selectId.equals("0") && !selectId.equals("1")) {
                    m.put("SORT", 1);
                } else {
                    m.put("SORT", sort);
                }
                view.addObject("FormComponent", m);
                view.addObject("treeId", selectId);
                return view;
            }
            Map<String, Object> singleFormComponent = DBHelper.queryForMap(" SELECT A.ID, A.LABLE_NAME, A.FIELD_NAME, A.DEFAULT_VALUE, A.SORT, A.MAX_LENGTH, A.MIN_LENGTH, " +
                    " A.COMPONENT_TYPE, A.ISDISABLED, A.FORM_TYPE, A.VALIDATE_RULES, A.POSITION_TYPE ," +
                    " A.COMPONENT_WIDTH, A.COMPONENT_LAYOUT, A.COMPONENT_URL, A.COMPONENT_URL_ATTR, " +
                    " A.SELECTCOMPONENTVALUE, A.SELECTCOMPONENTNAME, A.DOMID, A.AFTERORBEFORE,A.UPLOADID, A.UPLOADPOSITION, " +
                    " A.ISMULTICHOOSE, A.ATTACHMENTTYPE, A.DATETYPE, A.DATEFORMAT, A.TREETITLE, A.IDKEY, A.PIDKEY, A.NAMEKEY FROM FORM_EXTEND A WHERE ID=? ", id);
            view.addObject("FormComponent", singleFormComponent);
            view.addObject("treeId", '0');
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
            formService.extendAddOrUpdate(param);
            r.setMsg("操作成功！");
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

    /*
     * 表单项管理-表单类型下拉列表
     * */
    @RequestMapping(value = "/selectList")
    @ResponseBody
    public Object formSelectList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = formService.formListSelect();
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

    /*
     * 表单管理页面
     * */
    @RequestMapping(value = "/")
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("admin/manage/form/list");
        return view;
    }

    /*
     * 表单管理页面——table表格加载接口
     * */
    @RequestMapping(value = "/tableList")
    @ResponseBody
    public Object tableList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = formService.tableListLoad(param);
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
     * 表单管理页面——获取新增、编辑页面
     * */
    @RequestMapping(value = {"/add/{selectId}/{isView}/{id}", "/add/{selectId}/{isView}"})
    public ModelAndView add(@PathVariable String selectId, @PathVariable Boolean isView, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/form/add");
        view.addObject("isView", isView);
        try {
            if (StringUtils.isEmpty(id)) { //新增页面
                Integer sort = DBHelper.queryForScalar(" SELECT (MAX(SORT)+1) FROM SS_FORM WHERE PARENTID=? ", Integer.class, selectId);
                Map<String, Object> m = new HashMap<>();
                if (sort == null) {
                    m.put("SORT", 1);
                } else {
                    m.put("SORT", sort);
                }
                view.addObject("Form", m);
                view.addObject("treeId", selectId);
                return view;
            } else { //编辑页面
                Map<String, Object> singleForm = DBHelper.queryForMap(" SELECT A.ID, A.NAME, A.PARENTID, A.REMARK, A.STATUS, A.SORT, A.ONLYCODE FROM SS_FORM A WHERE A.ID=? ", id);
                view.addObject("Form", singleForm);
                view.addObject("treeId", '0');
                view.addObject("id", id);
                return view;
            }
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /*
     * 表单管理页面——唯一编码（用于禁用下拉框选项）
     * */
    @RequestMapping(value = {"/onlyCodeDisList", "/onlyCodeDisList/{id}"})
    @ResponseBody
    public ResultEntity onlyCodeDisList(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> onlyCodeList;
        try {
            if (StringUtils.isEmpty(id)) { //新增页面
                onlyCodeList = DBHelper.queryForList(" SELECT ONLYCODE FROM SS_FORM WHERE ONLYCODE IS NOT NULL AND ISDELETE <> ? AND STATUS=? ", AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
            } else {//编辑页面
                onlyCodeList = DBHelper.queryForList(" SELECT ONLYCODE FROM SS_FORM WHERE ID<>? AND ONLYCODE IS NOT NULL AND ISDELETE <> ? AND STATUS=? ", id, AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
            }
            r.setDataset(onlyCodeList);
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
     * 表单管理页面——新增、编辑
     * */
    @RequestMapping(value = "/addOrUpdate")
    @ResponseBody
    public Object addOrUpdate() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            formService.addOrUpdate(param);
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
     * 表单管理页面——删除（假删除）
     * */
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            formService.del(param);
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
     * 表单管理页面——彻底删除（真删除）
     * */
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            formService.realDel(param);
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

    /*
     * 表单管理-上级目录下拉列表
     * */
    @RequestMapping(value = "/parentDirectoryList")
    @ResponseBody
    public Object parentDirectoryList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = formService.parentDirectoryList();
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

    /*
     * 表单管理-节点树(禁用状态显示，common中禁用状态不显示)
     * */
    @RequestMapping(value = "/loadFormZTreeList")
    @ResponseBody
    public Object loadFormZTreeList() {
        ResultEntity r = new ResultEntity(1);
        //参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = formService.loadFormZTreeList(param);
            r.setDataset(list);
            r.setMsg("");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /*
     * 表单项管理-位置类型下拉框（所属表单select框选择后动态生成）
     * */
    @RequestMapping(value = "/positionSelectList")
    @ResponseBody
    public Object positionSelectList() {
        ResultEntity r = new ResultEntity(1);
        //参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = formService.positionSelectList(param);
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
}
