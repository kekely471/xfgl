package com.tonbu.framework.dao.support;


import com.tonbu.framework.util.JdbcUtils;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class ProcedureCallback {
    String str_sql = null;
    Map<Integer, Object[]> paramsMap = new LinkedHashMap();

    public ProcedureCallback() {
    }

    protected void registerInParameter() {
        this.paramsMap.put(this.paramsMap.size() + 1, null);
    }

    protected void registerOutParameter(String type, String name) {
        this.paramsMap.put(this.paramsMap.size() + 1, new Object[]{name, type});
    }

    public ProcedureResult doInCallback(Connection conn, String sql, Object... objects) throws SQLException {
        this.str_sql = StringUtils.defaultString(this.parse(sql), sql);
        CallableStatement cs = null;
        cs = conn.prepareCall(this.str_sql);

        try {
            for(int i = 0; i < this.paramsMap.size(); ++i) {
                Integer key = i + 1;
                Object[] p = (Object[])this.paramsMap.get(key);
                if (p == null) {
                    if (objects == null || i >= objects.length) {
                        throw new IllegalArgumentException("not enough parameter");
                    }

                    JdbcUtils.bindStatementInput(cs, key, objects[i]);
                } else {
                    String type = (String)p[1];
                    JdbcUtils.bindStatementOutput(cs, key, type);
                }
            }

            ProcedureResult var10 = this.getResult(cs, cs.execute());
            return var10;
        } finally {
            JdbcUtils.close(cs);
        }
    }

    public String parse(String sql) {
        return sql;
    }

    protected ProcedureResult getResult(CallableStatement cstmt, boolean executeResult) throws SQLException {
        Map<String, Object> retmap = new LinkedHashMap();
        int index = 0;
        if (executeResult) {
            ResultSet rs = null;

            try {
                rs = cstmt.getResultSet();
                StringBuilder var10001 = new StringBuilder("dataset_");
                int var12 = index + 1;
                retmap.put(var10001.append(String.valueOf(index)).toString(), JdbcUtils.convertResultSetToMapList(rs, false));
                JdbcUtils.close(rs);

                while(cstmt.getMoreResults()) {
                    rs = cstmt.getResultSet();
                    retmap.put("dataset_" + String.valueOf(var12++), JdbcUtils.convertResultSetToMapList(rs, false));
                    JdbcUtils.close(rs);
                }
            } finally {
                JdbcUtils.close(rs);
            }
        }

        for(int i = 0; i < this.paramsMap.size(); ++i) {
            Integer key = i + 1;
            Object[] p = (Object[])this.paramsMap.get(key);
            if (p != null) {
                String name = (String)p[0];
                String type = (String)p[1];
                retmap.put(name, JdbcUtils.convertType(cstmt, key, type));
            }
        }

        return new ProcedureResult(retmap);
    }
}
