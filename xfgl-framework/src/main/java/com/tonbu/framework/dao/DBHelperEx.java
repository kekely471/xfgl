package com.tonbu.framework.dao;

import com.tonbu.framework.Constants;
import com.tonbu.framework.config.PropertiesConfig;
import com.tonbu.framework.dao.dialect.Dialect;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.dao.support.ProcedureCallback;
import com.tonbu.framework.dao.support.ProcedureResult;
import com.tonbu.framework.dao.support.SQLCallback;
import com.tonbu.framework.util.ContainerUtils;
import com.tonbu.framework.util.ClassUtils;
import com.tonbu.framework.util.JdbcUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * 可动态传入数据库jdpc连接名称的升级类，用户多数据库切换
 * 修改人：金磊
 * 修改时间：2018-08-20
 */
public class DBHelperEx {
    protected static Logger logger = LogManager.getLogger(ClassUtils.class.getName());
    private static JdbcTemplate jdbcTemplate = null;
    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate = null;
    private static LobHandler lobHandler = null;


    public DBHelperEx() {

    }

    protected static JdbcTemplate getJdbcTemplate(String dbSource) {
        if (StringUtils.isEmpty(dbSource)) {
            String enableDs = PropertiesConfig.getProperty("spring.datasource.switch");
            dbSource = enableDs+"JdbcTemplate";

        }
        jdbcTemplate = (JdbcTemplate) ContainerUtils.getBean(Constants.DB_SOURCE_MAP.get(dbSource));
        return jdbcTemplate;
    }

    protected static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String db_source) {
        if (namedParameterJdbcTemplate == null) {
            if (jdbcTemplate == null) {
                jdbcTemplate = getJdbcTemplate(db_source);
            }

            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        }

        return namedParameterJdbcTemplate;
    }


    protected static LobHandler getLobHandler() {
        if (lobHandler == null) {
            lobHandler = (LobHandler) ContainerUtils.getBean("oracleLobHandler");
        }
        return lobHandler;
    }

    protected static Dialect getDialect(String dbSource) {
        //Map<String, Dialect> dialects = (Map)ContainerUtils.getBean("dialectProperties");
        //return dialects != null ? (Dialect)dialects.get("dataSource") : null;
        //修改为直接根据配置文件取方言信息
        try{
            return (Dialect)Class.forName("com.tonbu.framework.dao.dialect." +  Constants.DB_DIALECT_MAP.get(dbSource)).newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
      //  return (Dialect) ClassUtils.newInstance("com.tonbu.framework.dao.dialect." + Constants.DB_DIALECT_MAP.get(dbSource));

    }

    protected static BeanPropertySqlParameterSource paramBeanMapper(Object object) {
        return new BeanPropertySqlParameterSource(object);
    }

    protected static BeanPropertySqlParameterSource[] paramBeanMapper(List<? extends Object> list) {
        if (list == null) {
            return null;
        } else {
            BeanPropertySqlParameterSource[] sources = new BeanPropertySqlParameterSource[list.size()];

            for (int i = 0; i < list.size(); ++i) {
                sources[i] = paramBeanMapper(list.get(i));
            }

            return sources;
        }
    }


    protected static <T> Object execSQL(String sql, SQLCallback callback, Class<T> clz, Object... args) {
        Object result = null;
        if (callback == null) {
            throw new RuntimeException("Callback object must not be null");
        } else {
            long t1 = System.currentTimeMillis();

            try {
                result = callback.doInCallback(sql, args);
            } catch (SQLException var13) {
                throw new RuntimeException(var13.getMessage(), var13);
            } finally {
                long t2 = System.currentTimeMillis();
                logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (args != null && args.length != 0 ? ArrayUtils.toString(args) : ""));
            }

            return result;
        }
    }

    protected static ProcedureResult execProcedure(String sql, ProcedureCallback callback, String db_source, Object... args) {
        ProcedureResult result = null;
        Connection con = null;
        if (callback == null) {
            throw new RuntimeException("Callback object must not be null");
        } else {
            long t1 = System.currentTimeMillis();

            try {
                con = DataSourceUtils.getConnection(getJdbcTemplate(db_source).getDataSource());
                result = callback.doInCallback(con, sql, args);
            } catch (SQLException var13) {
                throw new RuntimeException(var13.getMessage(), var13);
            } finally {
                DataSourceUtils.releaseConnection(con, getJdbcTemplate(db_source).getDataSource());
                long t2 = System.currentTimeMillis();
                logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (args != null && args.length != 0 ? ArrayUtils.toString(args) : ""));
            }

            return result;
        }
    }

    public static String[] getColumnFields(String sql, String db_source, Object... args) throws DataAccessException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection con = null;
        List<String> list = new ArrayList();
        long t1 = System.currentTimeMillis();

        try {
            con = DataSourceUtils.getConnection(getJdbcTemplate(db_source).getDataSource());
            pstmt = con.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; ++i) {
                    pstmt.setObject(i + 1, args[i]);
                }
            }

            rs = pstmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                String name = meta.getColumnName(i);
                list.add(name);
            }
        } catch (SQLException var16) {
            throw new RuntimeException(var16.getMessage(), var16);
        } finally {
            JdbcUtils.close((Connection) null, rs, pstmt);
            DataSourceUtils.releaseConnection(con, getJdbcTemplate(db_source).getDataSource());
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (args != null && args.length != 0 ? ArrayUtils.toString(args) : ""));
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public static int execute(String sql, String db_source, Object... args) {
        return (Integer) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                Connection con = null;
                boolean var6 = false;

                Integer var12;
                try {
                    con = DataSourceUtils.getConnection(DBHelperEx.getJdbcTemplate(db_source).getDataSource());
                    pstmt = con.prepareStatement(sql);
                    if (args != null) {
                        for (int i = 0; i < args.length; ++i) {
                            if (args[i] != null && args[i].getClass().isArray()) {
                                if (Array.getLength(args[i]) > 1) {
                                    String key = (String) Array.get(args[i], 0);
                                    Object value = (String) Array.get(args[i], 1);
                                    if ("clob".equalsIgnoreCase(key)) {
                                        String tmpValuex = (String) value;
                                        DBHelperEx.getLobHandler().getLobCreator().setClobAsCharacterStream(pstmt, i + 1, new StringReader(tmpValuex), tmpValuex.length());
                                    } else if ("blob".equalsIgnoreCase(key)) {
                                        byte[] tmpValue = (byte[]) value;
                                        DBHelperEx.getLobHandler().getLobCreator().setBlobAsBytes(pstmt, i + 1, tmpValue);
                                    }
                                }
                            } else {
                                pstmt.setObject(i + 1, args[i]);
                            }
                        }
                    }

                    int count = pstmt.executeUpdate();
                    var12 = count;
                } catch (SQLException var15) {
                    throw new RuntimeException(var15.getMessage(), var15);
                } finally {
                    JdbcUtils.close((Connection) null, (ResultSet) rs, pstmt);
                    DataSourceUtils.releaseConnection(con, DBHelperEx.getJdbcTemplate(db_source).getDataSource());
                }

                return var12;
            }
        }, (Class) null, args);
    }

    public static int update(String sql, String db_source, Object... args) {
        return execute(sql, db_source, args);
    }

    public static int[] batch(String db_source, String... sql) {
        long t1 = System.currentTimeMillis();

        int[] var4;
        try {
            var4 = getJdbcTemplate(db_source).batchUpdate(sql);
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql);
        }

        return var4;
    }

    public static <T> int[] batch(String sql, String db_source, List<T> paramlist) {
        long t1 = System.currentTimeMillis();

        int[] var5;
        try {
            var5 = getNamedParameterJdbcTemplate(db_source).batchUpdate(sql, paramBeanMapper(paramlist));
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql);
        }

        return var5;
    }

    public static int update(String sql, String db_source, Map<String, ?> paramMap) {
        long t1 = System.currentTimeMillis();

        int var5;
        try {
            var5 = getNamedParameterJdbcTemplate(db_source).update(sql, paramMap);
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (paramMap == null ? "" : ArrayUtils.toString(paramMap)));
        }

        return var5;
    }

    public static <T> int update(String sql, String db_source, T paramBean) {
        long t1 = System.currentTimeMillis();

        int var5;
        try {
            var5 = getNamedParameterJdbcTemplate(db_source).update(sql, paramBeanMapper(paramBean));
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (paramBean == null ? "" : ArrayUtils.toString(paramBean)));
        }

        return var5;
    }

    public static <T> List<T> queryForList(String sql, final Class<T> clz, String db_source, Object... objects) {
        return (List) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                return DBHelperEx.getJdbcTemplate(db_source).query(sql, BeanPropertyRowMapper.newInstance(clz), args);
            }
        }, clz, objects);
    }

    public static <T> T queryForBean(String sql, Class<T> clz, String db_source, Object... objects) {
        List<T> list = queryForList(sql, clz, db_source, objects);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public static List<Map<String, Object>> queryForList(String sql, String db_source, Object... args) {
        return (List) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                return DBHelperEx.getJdbcTemplate(db_source).queryForList(sql, args);
            }
        }, (Class) null, args);
    }

    public static Map<String, Object> queryForMap(String sql, String db_source, Object... args) {
        List<Map<String, Object>> list = queryForList(sql, db_source, args);
        return list != null && list.size() > 0 ? (Map) list.get(0) : null;
    }

    public static Map<String, Object> queryForMap(String sql, String db_source, Map args) {
        List<Map<String, Object>> list = queryForList(sql, db_source, args);
        return list != null && list.size() > 0 ? (Map) list.get(0) : null;
    }

    public static <T> T queryForScalar(String sql, final Class<T> clz, String db_source, Object... args) {
        return (T) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                Object object = null;
                try {
                    object = jdbcTemplate.queryForObject(sql, clz, db_source, args);
                } catch (EmptyResultDataAccessException e) {
                    // e.printStackTrace(); // 可以选择打印信息
                    return null;
                }
                return object;
            }
        }, (Class) null, args);
    }

    public static Page<?> queryForPage(String sql, int pageNo, int pageSize, String db_source, Object... args) {
        return queryForPage(sql, pageNo, pageSize, 0, db_source, args);
    }

    public static Page<?> queryForPage(String sql, final int pageNo, final int pageSize, final int totalCount, String db_source, Object... args) {
        return (Page) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                Integer total = totalCount;
                Dialect dialect = DBHelperEx.getDialect(db_source);
                String cnt_sql = dialect.getCountString(sql);
                if (totalCount <= 0) {
                    total = (Integer) DBHelperEx.getJdbcTemplate(db_source).queryForObject(cnt_sql, Integer.class, args);
                }

                if (total < 1) {
                    return new Page();
                } else {
                    int startIndex = Page.getStartOfPage(pageNo, pageSize);
                    String page_sql = dialect.getLimitString(sql, startIndex, startIndex + pageSize);
                    DBHelperEx.logger.debug("PagedSQL: " + page_sql);
                    List<?> resultList = DBHelperEx.getJdbcTemplate(db_source).queryForList(page_sql, args);
                    return new Page(startIndex, total, pageSize, resultList);
                }
            }
        }, (Class) null, args);
    }

    public static ProcedureResult executeProcedure(String sql, String db_source, Object... paramArgs) {
        return execProcedure(sql, new ProcedureCallback() {
            public String parse(String sql) {
                String callstr = StringUtils.lowerCase(sql);
                String nameHead = StringUtils.substringBefore(callstr, "(");
                String paramterBody = StringUtils.substringBetween(callstr, "(", ")");
                StringBuffer callBuffer = new StringBuffer(50);
                if (!StringUtils.contains(sql, "call")) {
                    callBuffer.append("{ call ");
                }

                callBuffer.append(nameHead);
                if (paramterBody != null) {
                    callBuffer.append(" (");
                    String[] paramterArray = StringUtils.split(paramterBody, ",");

                    for (int i = 0; i < paramterArray.length; ++i) {
                        String p = StringUtils.trim(paramterArray[i]);
                        if (StringUtils.isNotBlank(p)) {
                            if (StringUtils.equals(p, "?")) {
                                this.registerInParameter();
                            } else {
                                String paramname = StringUtils.trim(StringUtils.substringBefore(p, "{"));
                                String paramtype = StringUtils.trim(StringUtils.substringBetween(p, "{", "}"));
                                paramname = StringUtils.defaultIfEmpty(paramname, "outvar");
                                paramtype = StringUtils.defaultIfEmpty(paramtype, "string");
                                this.registerOutParameter(paramtype, paramname);
                                paramterArray[i] = "?";
                            }
                        }
                    }

                    callBuffer.append(StringUtils.join(paramterArray, ","));
                    callBuffer.append(")");
                }

                callBuffer.append("}");
                return callBuffer.toString();
            }
        }, db_source, paramArgs);
    }

    public static void main(String[] args) {


        List<Map<String, Object>> l_m = DBHelperEx.queryForList("select * from user ", "");


        logger.error("123");

    }
}
