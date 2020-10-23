package com.tonbu.framework.dao.dialect;


public class OracleDialect extends Dialect {
    public OracleDialect() {
    }

    public String getLimitString(String sql, int limit, int offset) {
        sql = sql.trim();
        boolean isForUpdate = false;
        if (sql.toLowerCase().endsWith(" for update")) {
            sql = sql.substring(0, sql.length() - 11);
            isForUpdate = true;
        }

        StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);
        if (offset > 0) {
            pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
        } else {
            pagingSelect.append("select * from ( ");
        }

        pagingSelect.append(sql);
        if (offset > 0) {
            pagingSelect.append(" ) row_ ) where rownum_ <= " + offset + " and rownum_ > " + limit);
        } else {
            pagingSelect.append(" ) where rownum <= " + String.valueOf(limit));
        }

        if (isForUpdate) {
            pagingSelect.append(" for update");
        }

        return pagingSelect.toString();
    }

    public boolean supportsLimitOffset() {
        return true;
    }

    public String getCountString(String sql) {
        return "select count(1) as count from (" + sql + ")";
    }
}
