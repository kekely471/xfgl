package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.FormFrameService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FormFrameServiceImpl implements FormFrameService {

    Logger logger = LogManager.getLogger(FormFrameServiceImpl.class.getName());

    @Override
    public List extendTableListLoad(Map<String, String> param) {
        String key = param.get("key_name");
        String formType = param.get("formType");
        String formId = param.get("formId");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT A.ID, A.LABLE_NAME, A.FIELD_NAME, A.DEFAULT_VALUE, A.SORT, A.MAX_LENGTH, A.MIN_LENGTH, " +
                " A.COMPONENT_TYPE, B.USERNAME AS CREATE_ID, to_char(A.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, C.USERNAME AS UPDATE_ID, " +
                " to_char(A.UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss') UPDATE_TIME, A.ISDISABLED,A.FORM_TYPE, A.VALIDATE_RULES, " +
                " A.ISDELETE,A.COMPONENT_WIDTH, A.COMPONENT_LAYOUT, A.COMPONENT_URL, A.COMPONENT_URL_ATTR, A.SELECTCOMPONENTVALUE, A.SELECTCOMPONENTNAME, " +
                " A.UPLOADID, A.UPLOADPOSITION, A.ISMULTICHOOSE, A.ATTACHMENTTYPE, A.DATETYPE,A.TREETITLE, A.IDKEY, A.PIDKEY, A.NAMEKEY,A.SEARCH_URL,A.ISMULTIPLE," +
                "v.value as EXTEND_VALUE,(select d.name from ss_dict d where d.value = A.DATEFORMAT and d.type_code='layui_date_format')as DATEFORMAT" +
                " FROM SS_FORMFRAME A " +
                " LEFT JOIN SS_USER B ON B.ID=A.CREATE_ID " +
                " LEFT JOIN SS_USER C ON C.ID=A.UPDATE_ID " +
                " left join ss_formframe_value v on (v.form_extend_id=A.id and v.Form_Id=? ) " +
                " WHERE A.ISDISABLED = 1 AND A.ISDELETE <>" + AppConstant.STATUS_DEL);
        args.add(formId);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( A.LABLE_NAME LIKE ? ) ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(formType)) {
            sql.append(" AND A.FORM_TYPE = ? ");
            args.add(formType);
        }
        sql.append(" order by id asc");
        return DBHelper.queryForList(sql.toString(), args.toArray());
    }

    public Page<?> newExtendTableListLoad(Map<String, String> param) {
        String key = param.get("key_name");
        String formType = param.get("formType");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT A.ID, A.LABLE_NAME,A.FIELD_NAME, A.DEFAULT_VALUE, A.SORT, A.MAX_LENGTH, A.MIN_LENGTH, " +
                " A.COMPONENT_TYPE, B.USERNAME AS CREATE_ID, to_char(A.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, C.USERNAME AS UPDATE_ID, " +
                " to_char(A.UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss') UPDATE_TIME, A.ISDISABLED,A.FORM_TYPE, A.VALIDATE_RULES, " +
                " A.ISDELETE,A.COMPONENT_WIDTH, A.COMPONENT_LAYOUT, A.COMPONENT_URL, A.COMPONENT_URL_ATTR, A.SELECTCOMPONENTVALUE, A.SELECTCOMPONENTNAME, " +
                " A.UPLOADID, A.UPLOADPOSITION, A.ISMULTICHOOSE, A.ATTACHMENTTYPE, A.DATETYPE, A.DATEFORMAT, A.TREETITLE, A.IDKEY, A.PIDKEY, A.NAMEKEY " +
                " FROM SS_FORMFRAME A " +
                " LEFT JOIN SS_USER B ON B.ID=A.CREATE_ID " +
                " LEFT JOIN SS_USER C ON C.ID=A.UPDATE_ID " +
                " WHERE A.ISDELETE <>" + AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( A.LABLE_NAME LIKE ? ) ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(formType)) {
            sql.append(" AND A.FORM_TYPE = ? ");
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
        String dataType = param.get("dataType");
        if (StringUtils.isNotBlank(dataType)) {
            if (dataType.equals("1")) {
                param.put("componentUrl", "common/dict/system");
                param.put("componentUrlAttr", "type_code:" + param.get("type_code") + "");
            } else if (dataType.equals("2")) {
                param.put("componentUrl", "common/findDataByTable");
            } else if (dataType.equals("4")) {
                param.put("componentUrl", "common/findDataByTable");
                param.put("componentUrlAttr", "table_name:ss_user");
            } else if (dataType.equals("5")) {
                param.put("componentUrl", "common/findDataByTable");
                param.put("componentUrlAttr", "table_name:ss_dept");
            }
        }
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

    //SS_FORMFRAME表新增、修改时判断FIELD_NAME的唯一性
    public Boolean judgeOnlyName(Map<String, String> param, String addOrUpdate) {
        Integer numbs = 0;
        if (addOrUpdate.equals("add")) {
            numbs = DBHelper.queryForScalar(" SELECT COUNT(1) FROM SS_FORMFRAME WHERE ISDELETE<>? AND FORM_TYPE=? AND FIELD_NAME=? ",
                    Integer.class, AppConstant.STATUS_DEL, param.get("formType"), param.get("fieldName").trim());
        } else if (addOrUpdate.equals("update")) {
            numbs = DBHelper.queryForScalar(" SELECT COUNT(1) FROM SS_FORMFRAME WHERE ISDELETE<>? AND FORM_TYPE=? AND FIELD_NAME=? AND ID<>?",
                    Integer.class, AppConstant.STATUS_DEL, param.get("formType"), param.get("fieldName").trim(), param.get("id"));
        }
        if (numbs == 0) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * SS_FORMFRAME表新增（返回受影响的行数）
     * */
    public Integer extendAddSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" INSERT INTO SS_FORMFRAME(ID, LABLE_NAME, FIELD_NAME, DEFAULT_VALUE, SORT, MAX_LENGTH, MIN_LENGTH, COMPONENT_TYPE, " +
                " CREATE_ID, CREATE_TIME, ISDISABLED, FORM_TYPE, VALIDATE_RULES, ISDELETE, " +
                " COMPONENT_WIDTH, COMPONENT_LAYOUT, COMPONENT_URL, COMPONENT_URL_ATTR, SELECTCOMPONENTVALUE, " +
                " SELECTCOMPONENTNAME, UPLOADID, UPLOADPOSITION, ISMULTICHOOSE, ATTACHMENTTYPE, DATETYPE, DATEFORMAT," +
                " TREETITLE, IDKEY, PIDKEY, NAMEKEY,DATATYPE,TYPE_CODE,SEARCH_URL,ISMULTIPLE) " +
                " VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("id"), param.get("lableName"), param.get("fieldName"), param.get("defaultValue"), param.get("sort"),
                param.get("maxLength"), param.get("minLength"), param.get("componentType"), u.getUser_id(), param.get("isDisabled"), param.get("formType"),
                param.get("validateRules"), param.get("isDelete"), param.get("componentWidth"), param.get("componentLayout"), param.get("componentUrl"),
                param.get("componentUrlAttr"), param.get("selectComponentValue"), param.get("selectComponentName"), param.get("uploadId"),
                param.get("uploadPosition"), param.get("isMultiChoose"), param.get("attachmentType"), param.get("dateType"), param.get("dateFormat"),
                param.get("treeTitle"), param.get("idKey"), param.get("pIdKey"), param.get("nameKey"), param.get("dataType"), param.get("type_code"),
                param.get("SEARCH_URL"),param.get("ISMULTIPLE"));
        return effectRows;
    }

    /*
     * SS_FORMFRAME表修改（返回受影响的行数）
     * */
    public Integer extendUpdateSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE SS_FORMFRAME SET LABLE_NAME=?, FIELD_NAME=?, DEFAULT_VALUE=?, SORT=?, MAX_LENGTH=?, MIN_LENGTH=?, COMPONENT_TYPE=?, " +
                " UPDATE_ID=?, UPDATE_TIME=SYSDATE, ISDISABLED=?, FORM_TYPE=?, VALIDATE_RULES=?, COMPONENT_WIDTH=?, COMPONENT_LAYOUT=?, COMPONENT_URL=?, COMPONENT_URL_ATTR=?, SELECTCOMPONENTVALUE=?, SELECTCOMPONENTNAME=?, " +
                " UPLOADID=?, UPLOADPOSITION=?, ISMULTICHOOSE=?, ATTACHMENTTYPE=?, DATETYPE=?, DATEFORMAT=?, TREETITLE=?, IDKEY=?, PIDKEY=?, NAMEKEY=?,DATATYPE=?,TYPE_CODE=?,SEARCH_URL=?,ISMULTIPLE=? " +
                " WHERE ID=? ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("lableName"), param.get("fieldName"), param.get("defaultValue"), param.get("sort"),
                param.get("maxLength"), param.get("minLength"), param.get("componentType"), u.getUser_id(), param.get("isDisabled"), param.get("formType"),
                param.get("validateRules"), param.get("componentWidth"), param.get("componentLayout"), param.get("componentUrl"),
                param.get("componentUrlAttr"), param.get("selectComponentValue"), param.get("selectComponentName"),
                param.get("uploadId"), param.get("uploadPosition"), param.get("isMultiChoose"), param.get("attachmentType"),
                param.get("dateType"), param.get("dateFormat"), param.get("treeTitle"), param.get("idKey"), param.get("pIdKey"),
                param.get("nameKey"), param.get("dataType"), param.get("type_code"), param.get("SEARCH_URL"),param.get("ISMULTIPLE"), param.get("id"));
        return effectRows;
    }

    /*
     * SS_FORMFRAME表序列
     * */
    public Integer getExtendCurrentId() {
        Integer currentId = DBHelper.queryForScalar("SELECT SS_FORMFRAME_SEQ.NEXTVAL FROM DUAL", Integer.class);
        return currentId;
    }

    @Override
    public ResultEntity extendDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" UPDATE SS_FORMFRAME SET ISDELETE=" + AppConstant.STATUS_DEL +
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
        StringBuffer sql = new StringBuffer(" DELETE FROM SS_FORMFRAME " +
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

    /**
     * 查看数据库表是否存在
     *
     * @param tablename
     * @return
     */
    public int checkTableNameIsExist(String tablename) {
        return DBHelper.queryForScalar("select count(*) from user_tables where table_name =upper(?)", Integer.class, tablename);
    }
}
