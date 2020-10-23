package com.tonbu.framework;

import java.util.HashMap;
import java.util.Map;

public class Constants  {

    //账户状态
    public static final String ACCOUNT_ENABLED = "1";//可用
    public static final String ACCOUNT_DISABLED = "0";//已禁用(未发布)
    public static final String ACCOUNT_DEL = "-1";//删除

    public static final String INIT_PASSWORD = "666666";
    public static final String PASSWORD_STRDICT = "tonbu-soft";
    public static final String PASSWORD_COMMONS = "tonbusoft`12345678";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String MYSQL_FORMATDATE_FULL="%Y-%m-%d %H:%i:%s";
    public static final String MYSQL_FORMATDATE_SHORT="%Y-%m-%d";

    public static final String ORACLE_FORMATDATE_FULL="YYYY-MM-DD hh24:mi:ss";
    public static final String ORACLE_FORMATDATE_SHORT="YYYY-MM-DD";

    public static final Map<String,String> DB_SOURCE_MAP=new HashMap<String,String>(3);
    public static final Map<String,String> DB_DIALECT_MAP=new HashMap<String,String>(3);

    // TODO: 2019-1-23 这里的配置是暂时的，今后有空需要改成从数据库读取配置的模式 jinlei
    static {
        DB_SOURCE_MAP.put("primaryJdbcTemplate", "primaryJdbcTemplate");
        DB_SOURCE_MAP.put("secondaryJdbcTemplate", "secondaryJdbcTemplate");
        DB_SOURCE_MAP.put("thirdlyJdbcTemplate", "thirdlyJdbcTemplate");
        //MySQLDialect OracleDialect DB2Dialect
        DB_DIALECT_MAP.put("primaryJdbcTemplate", "MySQLDialect");
        DB_DIALECT_MAP.put("secondaryJdbcTemplate", "OracleDialect");
        DB_DIALECT_MAP.put("thirdlyJdbcTemplate", "SqlServerDialect");
    }

    public Constants() {

    }
}
