package com.tonbu.framework.dao.dialect;

public class MySQLDialect extends Dialect {
    public MySQLDialect() {
    }

    public String getLimitString(String sql, int limit, int offset) {
        return offset == 0 ? sql + " limit " + Integer.toString(offset) : sql + " limit " + Integer.toString(limit) + "," + Integer.toString(offset - limit);
    }

    public boolean supportsLimitOffset() {
        return true;
    }

    public String getCountString(String sql) {
        return "select count(1) as count from (" + sql + ") tmp";
    }
}
