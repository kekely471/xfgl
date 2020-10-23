package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.LogService;
import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LogServiceImpl implements LogService {


    public Page<?> loadLoginLogs(Map<String, String> param) {
        String key = param.get("key_name");
        // 获取sql 跟传入参数 不需要过滤是否启用
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.TYPE,V.LOGINIP,V.LOGINNAME,V.USERNAME,D.DEPTNAME PNAME,D.DEPTNO PDEPTNO, TO_CHAR(V.LOGINTIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') LOGINTIME  ");
        sql.append(" FROM SS_LOG_LOGIN V ");
        sql.append(" LEFT JOIN SS_USER U ON U.ID=V.USER_ID ");
        sql.append(" LEFT JOIN SS_DEPT D ON D.ID=U.DEPT_ID ");
        sql.append(" WHERE 1=1 "); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.USERNAME LIKE ? OR D.DEPTNAME LIKE ? OR V.LOGINNAME LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.LOGINTIME DESC");
        }
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

    /**
     * 删除
     */
    public void realDelLogin(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM SS_LOG_LOGIN WHERE ID = ? ", id_s[i]);
        }
    }

    public Page<?> loadOpLogs(Map<String, String> param) {
        String key = param.get("key_name");
        // 获取sql 跟传入参数 不需要过滤是否启用
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.URL,V.USER_IP,A.LOGINNAME,V.USER_NAME,V.PARAM,V.MODEL_NAME,V.USER_APP,V.OPERATION_TYPE,D.DEPTNAME PNAME,D.DEPTNO PDEPTNO,V.RESPONSE_INFO, " +
                "TO_CHAR(V.OPERATION_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') OPERATIONTIME  ");
        sql.append(" FROM SS_LOG_OPERATE V ");
        sql.append(" LEFT JOIN SS_USER U ON U.ID=V.USER_ID ");
        sql.append(" LEFT JOIN ACCOUNT_USER AU ON U.ID=AU.USER_ID ");
        sql.append(" LEFT JOIN ACCOUNT A ON A.ID=AU.ACCOUNT_ID ");
        sql.append(" LEFT JOIN SS_DEPT D ON D.ID=U.DEPT_ID ");
        sql.append(" WHERE 1=1 "); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.USER_NAME LIKE ? OR D.DEPTNAME LIKE ?  OR A.LOGINNAME LIKE ? OR V.MODEL_NAME LIKE ?) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.OPERATION_TIME DESC");
        }
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }


    /**
     * 删除
     */
    public void realDelOp(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM SS_LOG_OPERATE WHERE ID = ? ", id_s[i]);
        }
    }

    public Page<?> loadBusinessLogs(Map<String, String> param) {
        String key = param.get("key_name");
        // 获取sql 跟传入参数 不需要过滤是否启用
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("  SELECT V.ID,V.TYPE,V.CREATE_ID,V.CONTENT,V.IP,V.USERNAME,V.LOGINNAME,TO_CHAR(V.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') CREATETIME   FROM B_LOG V ");
        sql.append(" WHERE 1=1 "); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.TYPE LIKE ? OR V.CONTENT LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.CREATE_TIME DESC");
        }
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

}
