package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.FormService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class FormServiceImpl implements FormService {

    Logger logger = LogManager.getLogger(FormServiceImpl.class.getName());

    @Override
    public Page<?> extendTableListLoad(Map<String, String> param) {
        String key = param.get("key_name");
        String formType = param.get("formId");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT A.ID, A.LABLE_NAME, A.FIELD_NAME, A.DEFAULT_VALUE, A.SORT, A.MAX_LENGTH, A.MIN_LENGTH, " +
                " A.COMPONENT_TYPE, B.USERNAME AS CREATE_ID, to_char(A.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, C.USERNAME AS UPDATE_ID, " +
                " to_char(A.UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss') UPDATE_TIME, A.ISDISABLED, D.NAME AS FORM_TYPE, A.VALIDATE_RULES, " +
                " E.NAME AS POSITION_TYPE, A.ISDELETE,A.COMPONENT_WIDTH, A.COMPONENT_LAYOUT, A.COMPONENT_URL, A.COMPONENT_URL_ATTR, A.SELECTCOMPONENTVALUE, " +
                " A.SELECTCOMPONENTNAME, A.DOMID, A.AFTERORBEFORE, A.UPLOADID, A.UPLOADPOSITION, A.ISMULTICHOOSE, A.ATTACHMENTTYPE, A.DATETYPE, A.DATEFORMAT, A.TREETITLE, A.IDKEY, A.PIDKEY, A.NAMEKEY " +
                " FROM FORM_EXTEND A " +
                " LEFT JOIN SS_USER B ON B.ID=A.CREATE_ID " +
                " LEFT JOIN SS_USER C ON C.ID=A.UPDATE_ID " +
                " LEFT JOIN SS_FORM D ON D.ID=A.FORM_TYPE " +
                " LEFT JOIN SS_FORM E ON E.ID=A.POSITION_TYPE " +
                " WHERE A.ISDELETE <>" + AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( A.LABLE_NAME LIKE ? ) ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(formType)) {
            sql.append(" AND ( A.FORM_TYPE = ? OR D.PARENTID = ? OR A.POSITION_TYPE=?) ");
            args.add(formType);
            args.add(formType);
            args.add(formType);
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void extendAddOrUpdate(Map<String, String> param) {
        String id = param.get("id");
        String isDisabled = param.get("isDisabled") == null ? "0" : "1";//状态
        param.put("isDisabled", isDisabled);
        if (StringUtils.isBlank(id)) {// 新增
            param.put("id", getExtendCurrentId().toString());
            param.put("isDelete", "1");
            if (judgeOnlyName(param, "add")) {
                if (extendAddSql(param) != 1) {
                    throw new JSONException("新增失败! ");
                }
            } else {
                throw new JSONException("新增失败!(字段名已经存在)");
            }
        } else {// 修改
            if (judgeOnlyName(param, "update")) {
                if (extendUpdateSql(param) != 1) {
                    throw new JSONException("修改失败! ");
                }
            } else {
                throw new JSONException("修改失败!(字段名已经存在)");
            }
        }
    }

    //FORM_EXTEND表新增、修改时判断FIELD_NAME的唯一性
    public Boolean judgeOnlyName(Map<String, String> param, String addOrUpdate) {
        Integer numbs = 0;
        if (addOrUpdate.equals("add")) {
            numbs = DBHelper.queryForScalar(" SELECT COUNT(1) FROM FORM_EXTEND WHERE ISDELETE<>? AND FORM_TYPE=? AND POSITION_TYPE=? AND FIELD_NAME=? ",
                    Integer.class, AppConstant.STATUS_DEL, param.get("formType"), param.get("positionType"), param.get("fieldName").trim());
        } else if (addOrUpdate.equals("update")) {
            numbs = DBHelper.queryForScalar(" SELECT COUNT(1) FROM FORM_EXTEND WHERE ISDELETE<>? AND FORM_TYPE=? AND POSITION_TYPE=? AND FIELD_NAME=? AND ID<>?",
                    Integer.class, AppConstant.STATUS_DEL, param.get("formType"), param.get("positionType"), param.get("fieldName").trim(), param.get("id"));
        }
        if (numbs == 0) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * FORM_EXTEND表新增（返回受影响的行数）
     * */
    public Integer extendAddSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" INSERT INTO FORM_EXTEND(ID, LABLE_NAME, FIELD_NAME, DEFAULT_VALUE, SORT, MAX_LENGTH, MIN_LENGTH, COMPONENT_TYPE, " +
                " CREATE_ID, CREATE_TIME, ISDISABLED, FORM_TYPE, VALIDATE_RULES, POSITION_TYPE, ISDELETE, " +
                " COMPONENT_WIDTH, COMPONENT_LAYOUT, COMPONENT_URL, COMPONENT_URL_ATTR, SELECTCOMPONENTVALUE, SELECTCOMPONENTNAME, DOMID, AFTERORBEFORE, " +
                " UPLOADID, UPLOADPOSITION, ISMULTICHOOSE, ATTACHMENTTYPE, DATETYPE, DATEFORMAT, TREETITLE, IDKEY, PIDKEY, NAMEKEY) " +
                " VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("id"), param.get("lableName"), param.get("fieldName"), param.get("defaultValue"), param.get("sort"),
                param.get("maxLength"), param.get("minLength"), param.get("componentType"), u.getUser_id(), param.get("isDisabled"), param.get("formType"),
                param.get("validateRules"), param.get("positionType"), param.get("isDelete"), param.get("componentWidth"), param.get("componentLayout"), param.get("componentUrl"),
                param.get("componentUrlAttr"), param.get("selectComponentValue"), param.get("selectComponentName"), param.get("domId"), param.get("afterOrBefore"),
                param.get("uploadId"), param.get("uploadPosition"), param.get("isMultiChoose"), param.get("attachmentType"), param.get("dateType"), param.get("dateFormat"),
                param.get("treeTitle"), param.get("idKey"), param.get("pIdKey"), param.get("nameKey"));
        return effectRows;
    }

    /*
     * FORM_EXTEND表修改（返回受影响的行数）
     * */
    public Integer extendUpdateSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE FORM_EXTEND SET LABLE_NAME=?, FIELD_NAME=?, DEFAULT_VALUE=?, SORT=?, MAX_LENGTH=?, MIN_LENGTH=?, COMPONENT_TYPE=?, " +
                " UPDATE_ID=?, UPDATE_TIME=SYSDATE, ISDISABLED=?, FORM_TYPE=?, VALIDATE_RULES=?, POSITION_TYPE=? ,COMPONENT_WIDTH=?, COMPONENT_LAYOUT=?, COMPONENT_URL=?, COMPONENT_URL_ATTR=?, SELECTCOMPONENTVALUE=?, SELECTCOMPONENTNAME=?, DOMID=?, AFTERORBEFORE=?, " +
                " UPLOADID=?, UPLOADPOSITION=?, ISMULTICHOOSE=?, ATTACHMENTTYPE=?, DATETYPE=?, DATEFORMAT=?, TREETITLE=?, IDKEY=?, PIDKEY=?, NAMEKEY=? " +
                " WHERE ID=? ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("lableName"), param.get("fieldName"), param.get("defaultValue"), param.get("sort"),
                param.get("maxLength"), param.get("minLength"), param.get("componentType"), u.getUser_id(), param.get("isDisabled"), param.get("formType"),
                param.get("validateRules"), param.get("positionType"), param.get("componentWidth"), param.get("componentLayout"), param.get("componentUrl"),
                param.get("componentUrlAttr"), param.get("selectComponentValue"), param.get("selectComponentName"), param.get("domId"), param.get("afterOrBefore"),
                param.get("uploadId"), param.get("uploadPosition"), param.get("isMultiChoose"), param.get("attachmentType"), param.get("dateType"), param.get("dateFormat"),
                param.get("treeTitle"), param.get("idKey"), param.get("pIdKey"), param.get("nameKey"), param.get("id"));
        return effectRows;
    }

    /*
     * FORM_EXTEND表序列
     * */
    public Integer getExtendCurrentId() {
        Integer currentId = DBHelper.queryForScalar("SELECT FORM_EXTEND_SEQ.NEXTVAL FROM DUAL", Integer.class);
        return currentId;
    }

    @Override
    public ResultEntity extendDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" UPDATE FORM_EXTEND SET ISDELETE=" + AppConstant.STATUS_DEL +
                " WHERE ID=? ");
        ResultEntity r = new ResultEntity(1);
        try {
            for (int i = 0; i < id_s.length; i++) {
                Integer affectRow = DBHelper.execute(sql.toString(), id_s[i]);
                if (affectRow != 1) {
                    throw new JSONException("编号为" + (id_s[i]) + "的信息删除失败 ");
                }
            }
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @Override
    public ResultEntity extendRealDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" DELETE FROM FORM_EXTEND " +
                " WHERE ID=? ");
        ResultEntity r = new ResultEntity(1);
        try {
            for (int i = 0; i < id_s.length; i++) {
                Integer affectRow = DBHelper.execute(sql.toString(), id_s[i]);
                if (affectRow != 1) {
                    throw new JSONException("编号为" + (id_s[i]) + "的信息彻底删除失败 ");
                }
            }
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @Override
    public List<Map<String, Object>> formListSelect() {
        //数据库SS_FORM表单根Id为1（子级parentId）
        Integer parentId = 1;
        return DBHelper.queryForList(" SELECT ID,NAME FROM SS_FORM WHERE PARENTID=? AND ISDELETE <> ? AND STATUS =? ORDER BY SORT ", parentId, AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
    }

    @Override
    public Page<?> tableListLoad(Map<String, String> param) {
        String key = param.get("key_name");
        String formId = param.get("formId");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT A.ID, A.NAME, D.NAME AS PARENTID, A.REMARK, A.STATUS, B.USERNAME AS CREATE_ID, " +
                " to_char(A.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, C.USERNAME AS UPDATE_ID, " +
                " to_char(A.UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss') UPDATE_TIME, A.SORT,A.ONLYCODE, A.PARENTID AS FORMID FROM SS_FORM A " +
                " LEFT JOIN SS_USER B ON B.ID=A.CREATE_ID " +
                " LEFT JOIN SS_USER C ON C.ID=A.UPDATE_ID " +
                " LEFT JOIN SS_FORM D ON D.ID=A.PARENTID " +
                " WHERE A.ISDELETE <>" + AppConstant.STATUS_DEL);
        //SS_FORM根目录ID为1,禁止（增、删、改）操作
        sql.append(" AND A.ID <> 1 ");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( A.NAME LIKE ? ) ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(formId)) {
            sql.append(" AND ( A.ID = ? OR A.PARENTID = ? ) ");
            args.add(formId);
            args.add(formId);
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void addOrUpdate(Map<String, String> param) {
        String id = param.get("id");
        String status = param.get("status") == null ? "0" : "1";//状态
        param.put("status", status);
        if (StringUtils.isBlank(id)) {// 新增
            //名称唯一性校验
            Integer count = DBHelper.queryForScalar(" SELECT COUNT(1) FROM SS_FORM WHERE NAME=? AND PARENTID=? " +
                            " AND ISDELETE <> ? AND STATUS =? ",
                    Integer.class, param.get("name"), param.get("parentId"), AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
            if (count > 0) {
                throw new JSONException("新增失败！（该名称已经存在） ");
            } else {
                param.put("id", getCurrentId().toString());
                //-1为假删除,其他为未删除状态
                param.put("isDelete", "1");
                if (addSql(param) != 1) {
                    throw new JSONException("新增失败 ");
                }
            }
        } else {// 修改
            //名称唯一性校验
            Integer count = DBHelper.queryForScalar(" SELECT COUNT(1) FROM SS_FORM WHERE NAME=? AND PARENTID=? AND ID <> ? " +
                            " AND ISDELETE <> ? AND STATUS =? ",
                    Integer.class, param.get("name"), param.get("parentId"), param.get("id"), AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
            if (count > 0) {
                throw new JSONException("修改失败！（该名称已经存在） ");
            } else {
                if (updateSql(param) != 1) {
                    throw new JSONException("修改失败 ");
                }
            }
        }
    }

    /*
     * SS_FORM表新增（返回受影响的行数）
     * */
    public Integer addSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" INSERT INTO SS_FORM(ID, NAME, PARENTID, REMARK, STATUS, CREATE_ID, CREATE_TIME, SORT, ISDELETE, ONLYCODE) " +
                " VALUES(?,?,?,?,?,?,SYSDATE,?,?,?) ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("id"), param.get("name"), param.get("parentId"), param.get("remark"), param.get("status"),
                u.getUser_id(), param.get("sort"), param.get("isDelete"), param.get("onlyCode"));
        return effectRows;
    }

    /*
     * SS_FORM表修改（返回受影响的行数）
     * */
    public Integer updateSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE SS_FORM SET NAME=?, PARENTID=?, REMARK=?, STATUS=?, UPDATE_ID=?, UPDATE_TIME=SYSDATE, SORT=?, ONLYCODE=? WHERE ID=? ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("name"), param.get("parentId"), param.get("remark"), param.get("status"),
                u.getUser_id(), param.get("sort"), param.get("OnlyCode"), param.get("id"));
        return effectRows;
    }

    /*
     * SS_FORM表序列
     * */
    public Integer getCurrentId() {
        Integer currentId = DBHelper.queryForScalar("SELECT SS_FORM_SEQ.NEXTVAL FROM DUAL", Integer.class);
        return currentId;
    }

    @Override
    public ResultEntity del(Map<String, String> param) {
        String list = param.get("list");
        String[] list_s = list.split("\\,");
        StringBuffer sql = new StringBuffer(" UPDATE SS_FORM SET ISDELETE=" + AppConstant.STATUS_DEL +
                " WHERE ID=? ");
        //删除子集数据（假删除）
        StringBuffer sonSql = new StringBuffer(" UPDATE SS_FORM SET ISDELETE=" + AppConstant.STATUS_DEL +
                " WHERE PARENTID=? ");
        ResultEntity r = new ResultEntity(1);
        try {
            for (int i = 0; i < list_s.length; i++) {
                String id = list_s[i].split("\\+")[0];
                String parentId = list_s[i].split("\\+")[1];
                Integer affectRow = DBHelper.execute(sql.toString(), id);
                if (affectRow != 1) {
                    throw new JSONException("编号为" + id + "的信息删除失败 ");
                } else if (affectRow == 1) {
                    if (parentId.equals("1")) {//删除子集数据
                        //更改关联表Form_Extend数据状态（假删除）
                        StringBuffer extendSql = new StringBuffer(" UPDATE FORM_EXTEND SET ISDELETE=" + AppConstant.STATUS_DEL +
                                " WHERE FORM_TYPE=? ");
                        DBHelper.execute(extendSql.toString(), id);
                    } else if (!parentId.equals("1")) {
                        //更改关联表Form_Extend数据状态（假删除）
                        StringBuffer extendSql = new StringBuffer(" UPDATE FORM_EXTEND SET ISDELETE=" + AppConstant.STATUS_DEL +
                                " WHERE FORM_TYPE=? AND POSITION_TYPE=? ");
                        DBHelper.execute(extendSql.toString(), parentId, id);
                    }
                }
            }
            for (int i = 0; i < list_s.length; i++) {
                String id = list_s[i].split("\\+")[0];
                DBHelper.execute(sonSql.toString(), id);
            }
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @Override
    public ResultEntity realDel(Map<String, String> param) {
        String list = param.get("list");
        String[] list_s = list.split("\\,");
        StringBuffer sql = new StringBuffer(" DELETE FROM SS_FORM " +
                " WHERE ID=? ");
        //删除子集数据（真删除）
        StringBuffer sonSql = new StringBuffer(" DELETE FROM SS_FORM " +
                " WHERE PARENTID=? ");
        ResultEntity r = new ResultEntity(1);
        try {
            for (int i = 0; i < list_s.length; i++) {
                String id = list_s[i].split("\\+")[0];
                String parentId = list_s[i].split("\\+")[1];
                Integer affectRow = DBHelper.execute(sql.toString(), id);
                if (affectRow != 1) {
                    throw new JSONException("编号为" + id + "的信息删除失败 ");
                } else if (affectRow == 1) {
                    DBHelper.execute(sonSql.toString(), id);
                    if (parentId.equals("1")) {//删除子集数据
                        //更改关联表Form_Extend数据状态（真删除）
                        StringBuffer extendSql = new StringBuffer(" DELETE FROM FORM_EXTEND " +
                                " WHERE FORM_TYPE=? ");
                        DBHelper.execute(extendSql.toString(), id);
                    } else if (!parentId.equals("1")) {
                        //更改关联表Form_Extend数据状态（真删除）
                        StringBuffer extendSql = new StringBuffer(" DELETE FROM FORM_EXTEND " +
                                " WHERE FORM_TYPE=? AND POSITION_TYPE=? ");
                        DBHelper.execute(extendSql.toString(), parentId, id);
                    }
                }
            }
            for (int i = 0; i < list_s.length; i++) {
                String id = list_s[i].split("\\+")[0];
                DBHelper.execute(sonSql.toString(), id);
            }
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @Override
    public List<Map<String, Object>> parentDirectoryList() {
        //数据库SS_FORM表单根Id为1（子级parentId）
        Integer parentId = 1;
        return DBHelper.queryForList(" SELECT ID,NAME FROM SS_FORM WHERE (ID=? OR PARENTID=?) AND ISDELETE <> ? AND STATUS=? ORDER BY SORT ", parentId, parentId, AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
    }

    @Override
    public List<Map<String, Object>> loadFormZTreeList(Map<String, String> param) {
        return DBHelper.queryForList("SELECT ID,NAME,PARENTID FROM SS_FORM WHERE ISDELETE <> ? ORDER BY SORT", AppConstant.STATUS_DEL);
    }

    @Override
    public List<Map<String, Object>> positionSelectList(Map<String, String> param) {
        String formId = param.get("formId");
        return DBHelper.queryForList(" SELECT ID,NAME FROM SS_FORM WHERE PARENTID=? AND ISDELETE <> ? AND STATUS=? ORDER BY SORT ", formId, AppConstant.STATUS_DEL, AppConstant.STATUS_ENABLED);
    }
}
