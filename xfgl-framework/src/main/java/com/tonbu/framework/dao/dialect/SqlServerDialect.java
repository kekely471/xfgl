package com.tonbu.framework.dao.dialect;

public class SqlServerDialect extends Dialect {

    public SqlServerDialect(){

    }

    /**
     * sql语句必须有order by 仅支持sqlserver2012之后的数据库
     * @param sql
     * @param limit
     * @param offset
     * @return
     */
    public String getLimitString(String sql, int limit, int offset) {
        if(offset == 0){
            throw new RuntimeException("传入分页参数异常，请检查！");
        }else{
            return sql + "offset "+Integer.toString(limit)+" rows fetch next "+Integer.toString(offset - limit)+" rows only";
        }
    }

    public boolean supportsLimitOffset() {
        return true;
    }


    public String getCountString(String sql) {
        return "";
    }
}
