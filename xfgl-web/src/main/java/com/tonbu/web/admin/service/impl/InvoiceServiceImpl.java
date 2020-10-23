package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.CommonService;
import com.tonbu.web.admin.service.InvoiceService;
import com.tonbu.web.aop.AopFormValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    Logger logger = LogManager.getLogger(InvoiceServiceImpl.class.getName());

    @Autowired
    CommonService commonService;

    @Override
    public Page<?> tableListLoad(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT A.ID, A.INVOICETYPE, A.REMARK, B.USERNAME AS CREATE_ID, to_char(A.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, C.USERNAME AS UPDATE_ID, " +
                " to_char(A.UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss') UPDATE_TIME, A.ISDELETE, A.DRAWER, to_char(A.BILLINGTIME,'yyyy-MM-dd HH24:mi:ss') BILLINGTIME, A.ISHISTORY, A.INVOICENAME, A.CHECKCODE, " +
                " A.SHOPDEPTNAME, A.SHOPDEPTPOSITION, A.SHOPDEPTPHONE, A.SHOPDEPTBANKCODE, A.SELLDEPTNAME, A.SELLDEPTPOSITION, " +
                " A.SELLDEPTPHONE, A.SELLDEPTBANKCODE, A.GOODSORSERVICENAME, A.SPECIFICATIONTYPE, A.UNTI, A.GOODSNUMBER, A.SINGLEPRICE, " +
                " A.TAXRATE, A.PAYEE, A.REVIEWER " +
                " FROM INVOICE A " +
                " LEFT JOIN SS_USER B ON B.ID=A.CREATE_ID " +
                " LEFT JOIN SS_USER C ON C.ID=A.UPDATE_ID " +
                " WHERE A.ISDELETE <>" + AppConstant.STATUS_DEL +
                " AND A.ISHISTORY=1 ");
        if (StringUtils.isNotBlank(key)) {
            sql.append("AND ( A.INVOICENAME LIKE ? )");
            args.add("%" + key + "%");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /*
     * INVOICE表序列
     * */
    public Integer getCurrentId() {
        Integer currentId = DBHelper.queryForScalar(" SELECT INVOICE_SEQ.NEXTVAL FROM DUAL ", Integer.class);
        return currentId;
    }

    /*
     * FORM_EXTEND_VALUE表序列
     * */
    public Integer getFormExtendValueId() {
        Integer currentId = DBHelper.queryForScalar(" SELECT FORM_EXTEND_VALUE_SEQ.NEXTVAL FROM DUAL ", Integer.class);
        return currentId;
    }

    /*
     * 动态组件渲染数据
     * */
    public List<Map<String, Object>> getRenderingKey(Map<String, String> param) {
        //动态组件渲染数据
        StringBuffer renderingSQL = new StringBuffer(" SELECT C.* FROM SS_FORM A " +
                " INNER JOIN SS_FORM B " +
                " ON A.PARENTID=B.ID " +
                " INNER JOIN FORM_EXTEND C " +
                " ON A.ID=C.POSITION_TYPE " +
                " WHERE B.ONLYCODE=? " +
                " AND A.STATUS=1 " +
                " AND A.ISDELETE<>" + AppConstant.STATUS_DEL +
                " AND C.ISDISABLED=1 " +
                " AND C.ISDELETE<>" + AppConstant.STATUS_DEL);
        List<Map<String, Object>> renderingList = DBHelper.queryForList(renderingSQL.toString(), param.get("formOnlyCode"));
        return renderingList;
    }

    /*
     * INVOICE表新增（返回受影响的行数）
     * */
    public Integer addSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer("INSERT INTO INVOICE(ID, INVOICETYPE, REMARK, CREATE_ID, CREATE_TIME, ISDELETE, DRAWER, BILLINGTIME, " +
                " ISHISTORY, INVOICENAME, CHECKCODE, SHOPDEPTNAME, SHOPDEPTPOSITION, SHOPDEPTPHONE, SHOPDEPTBANKCODE, SELLDEPTNAME, SELLDEPTPOSITION, " +
                " SELLDEPTPHONE, SELLDEPTBANKCODE, GOODSORSERVICENAME, SPECIFICATIONTYPE, UNTI, GOODSNUMBER, SINGLEPRICE, TAXRATE, PAYEE, REVIEWER) " +
                " VALUES(?,?,?,?,SYSDATE,?,?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("id"), param.get("invoiceType"), param.get("remark"), u.getUser_id(), param.get("isDelete"), param.get("drawer"),
                param.get("isHistory"), param.get("invoiceName"), param.get("checkCode"), param.get("shopDeptName"), param.get("shopDeptPosition"),
                param.get("shopDeptPhone"), param.get("shopDeptBankCode"), param.get("sellDeptName"), param.get("sellDeptPosition"),
                param.get("sellDeptPhone"), param.get("sellDeptBankCode"), param.get("goodsOrServiceName"), param.get("specificationType"),
                param.get("unit"), param.get("goodsNumber"), param.get("singlePrice"), param.get("taxrate"), param.get("payee"), param.get("reviewer"));

//        if (effectRows > 0 && StringUtils.isNotBlank(param.get("formType"))) {
//            commonService.getRenderingKeyBySSFORMFRAME(param);
//        }
        return effectRows;
    }

    /*
     * INVOICE表修改（返回受影响的行数）
     * */

    public Integer updateSql(Map<String, String> param) {
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE INVOICE SET INVOICETYPE=?, REMARK=?, DRAWER=?, BILLINGTIME=SYSDATE, ISHISTORY=?, " +
                " INVOICENAME=?, CHECKCODE=?, SHOPDEPTNAME=?, SHOPDEPTPOSITION=?, SHOPDEPTPHONE=?, SHOPDEPTBANKCODE=?, SELLDEPTNAME=?, " +
                " SELLDEPTPOSITION=?, SELLDEPTPHONE=?, SELLDEPTBANKCODE=?, GOODSORSERVICENAME=?, SPECIFICATIONTYPE=?, UNTI=?, GOODSNUMBER=?, " +
                " SINGLEPRICE=?, TAXRATE=?, PAYEE=?, REVIEWER=? " +
                " where id=? ");
        Integer effectRows = DBHelper.execute(sql.toString(), param.get("invoiceType"), param.get("remark"), param.get("drawer"),
                param.get("isHistory"), param.get("invoiceName"), param.get("checkCode"), param.get("shopDeptName"), param.get("shopDeptPosition"),
                param.get("shopDeptPhone"), param.get("shopDeptBankCode"), param.get("sellDeptName"), param.get("sellDeptPosition"),
                param.get("sellDeptPhone"), param.get("sellDeptBankCode"), param.get("goodsOrServiceName"), param.get("specificationType"),
                param.get("unit"), param.get("goodsNumber"), param.get("singlePrice"), param.get("taxrate"), param.get("payee"), param.get("reviewer"), param.get("id"));
//        if (effectRows > 0 && StringUtils.isNotBlank(param.get("formType"))) {
//            commonService.getRenderingKeyBySSFORMFRAME(param);
//        }
        return effectRows;
    }

    @Override
    @Transactional(value = "primaryTransactionManager")
    @AopFormValue
    public void addOrUpdate(Map<String, String> param) {
        String id = param.get("id");
        if (StringUtils.isBlank(id)) {// 新增
            param.put("id", getCurrentId().toString());
            //-1为假删除,其他为未删除状态
            param.put("isDelete", "1");
            //是否为历史状态（0为历史版本，1为最新版本）
            param.put("isHistory", "1");
            if (addSql(param) != 1) {
                throw new JSONException("新增失败 ");
            }
        } else {// 修改
            //是否为历史状态（0为历史版本，1为最新版本）添加历史版本功能后改成0
            param.put("isHistory", "1");
            if (updateSql(param) != 1) {
                throw new JSONException("修改失败 ");
            }
        }
    }

    @Override
    public ResultEntity del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" UPDATE INVOICE SET ISDELETE=" + AppConstant.STATUS_DEL +
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
    public ResultEntity realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" DELETE FROM INVOICE WHERE ID=? ");
        //删除扩展项
        StringBuffer delSql = new StringBuffer(" DELETE FROM FORM_EXTEND_VALUE WHERE FORM_ID=? ");
        ResultEntity r = new ResultEntity(1);
        try {
            for (int i = 0; i < id_s.length; i++) {
                Integer affectRow = DBHelper.execute(sql.toString(), id_s[i]);
                if (affectRow != 1) {
                    throw new JSONException("编号为" + (id_s[i]) + "的信息彻底删除失败 ");
                }
                DBHelper.execute(delSql.toString(), id_s[i]);
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
}
