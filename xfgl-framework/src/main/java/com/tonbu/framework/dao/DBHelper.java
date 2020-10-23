package com.tonbu.framework.dao;

import com.tonbu.framework.annotation.Column;
import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import com.tonbu.framework.config.PropertiesConfig;
import com.tonbu.framework.dao.dialect.Dialect;
import com.tonbu.framework.dao.support.*;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.util.ContainerUtils;
import com.tonbu.framework.util.ClassUtils;
import com.tonbu.framework.util.JdbcUtils;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tonbu.framework.helper.StringHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.lob.LobHandler;


public class DBHelper {
    protected static Logger logger = LogManager.getLogger(ClassUtils.class.getName());
    private static JdbcTemplate jdbcTemplate = null;
    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate = null;
    private static LobHandler lobHandler = null;

    private static String dialect=PropertiesConfig.getProperty("spring.datasource.dialect");

    private static String enableDataSource;

    public DBHelper() {

    }

    protected static JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            String enableDs = PropertiesConfig.getProperty("spring.datasource.switch");
            enableDataSource = enableDs+"JdbcTemplate";
            jdbcTemplate = (JdbcTemplate) ContainerUtils.getBean(enableDataSource);
        }

        return jdbcTemplate;
    }

    public static boolean save(String tableName, Map<String,Object> record) {
        return saveByMap(tableName, "id", record);
    }
    public static boolean saveByMap(String tableName, String primaryKey, Map<String,Object> record) {
        return saveByMapJdbc(getJdbcTemplate(),tableName, primaryKey, record);
    }

    static boolean  saveByMapJdbc(JdbcTemplate jdbcTemplate,String tableName, String primaryKey, Map<String,Object> record) {
        String[] pKeys = primaryKey.split(",");
        List<Object> paras = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        forDbSave(tableName, pKeys, record, sql, paras);
        int result = jdbcTemplate.update(sql.toString(),paras.toArray());
        return result >= 1;
    }

    public  static void  forDbSave(String tableName, String[] pKeys, Map<String,Object> record, StringBuilder sql, List<Object> paras) {
        tableName = tableName.trim();
        trimPrimaryKeys(pKeys); // important

        sql.append("insert into ");
        sql.append(tableName).append("(");
        StringBuilder temp = new StringBuilder();
        temp.append(") values(");

        for (Map.Entry<String, Object> e : record.entrySet()) {
            if (paras.size() > 0) {
                sql.append(", ");
                temp.append(", ");
            }
            sql.append("").append(e.getKey()).append("");
            for(String key : pKeys) {
                if (key.equals(e.getKey())) {
                    temp.append(e.getValue());
                } else {
                    temp.append("?");
                    paras.add(e.getValue());
                }
            }
        }
        sql.append(temp.toString()).append(")");
    }
    public  static void trimPrimaryKeys(String[] pKeys) {
        for (int i=0; i<pKeys.length; i++) {
            pKeys[i] = pKeys[i].trim();
        }
    }

    public static boolean modify(String tableName, String primaryKey, Map<String,Object> record) {
        String[] pKeys = primaryKey.split(",");
        Object[] ids = new Object[pKeys.length];

        for (int i = 0; i < pKeys.length; i++) {
            ids[i] = record.get(pKeys[i].trim()); // .trim() is important!
            if (ids[i] == null)
                throw new ActiveRecordException("You can't update record without Primary Key, " + pKeys[i] + " can not be null.");
        }

        StringBuilder sql = new StringBuilder();
        List<Object> paras = new ArrayList<Object>();
        forDbUpdate(tableName, pKeys, ids, record, sql, paras);

        if (paras.size() <= 1) { // Needn't update
            return false;
        }

        return getJdbcTemplate().update(sql.toString(),paras.toArray())>=1;
    }

    public static void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Map<String,Object> record, StringBuilder sql, List<Object> paras) {
        tableName = tableName.trim();
        trimPrimaryKeys(pKeys);

        sql.append("update ").append(tableName).append(" set ");
        for (Map.Entry<String, Object> e : record.entrySet()) {
            String colName = e.getKey();
            if (!isPrimaryKey(colName, pKeys)) {
                if (paras.size() > 0) {
                    sql.append(", ");
                }
                sql.append(colName).append(" = ? ");
                paras.add(e.getValue());
            }
        }
        sql.append(" where ");
        for (int i = 0; i < pKeys.length; i++) {
            if (i > 0) {
                sql.append(" and ");
            }
            sql.append("").append(pKeys[i]).append(" = ?");
            paras.add(ids[i]);
        }
    }

    public static boolean isPrimaryKey(String colName, String[] pKeys) {
        for (String pKey : pKeys) {
            if (colName.equalsIgnoreCase(pKey)) {
                return true;
            }
        }
        return false;
    }


    protected static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            if (jdbcTemplate == null) {
                jdbcTemplate = getJdbcTemplate();
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

    protected static Dialect getDialect() {
        //Map<String, Dialect> dialects = (Map)ContainerUtils.getBean("dialectProperties");
        //return dialects != null ? (Dialect)dialects.get("dataSource") : null;
        //修改为直接根据配置文件取方言信息
        try{
            return (Dialect)Class.forName("com.tonbu.framework.dao.dialect." + dialect).newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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

    protected static ProcedureResult execProcedure(String sql, ProcedureCallback callback, Object... args) {
        ProcedureResult result = null;
        Connection con = null;
        if (callback == null) {
            throw new RuntimeException("Callback object must not be null");
        } else {
            long t1 = System.currentTimeMillis();

            try {
                con = DataSourceUtils.getConnection(getJdbcTemplate().getDataSource());
                result = callback.doInCallback(con, sql, args);
            } catch (SQLException var13) {
                throw new RuntimeException(var13.getMessage(), var13);
            } finally {
                DataSourceUtils.releaseConnection(con, getJdbcTemplate().getDataSource());
                long t2 = System.currentTimeMillis();
                logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (args != null && args.length != 0 ? ArrayUtils.toString(args) : ""));
            }

            return result;
        }
    }

    public static String[] getColumnFields(String sql, Object... args) throws DataAccessException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection con = null;
        List<String> list = new ArrayList();
        long t1 = System.currentTimeMillis();

        try {
            con = DataSourceUtils.getConnection(getJdbcTemplate().getDataSource());
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
            DataSourceUtils.releaseConnection(con, getJdbcTemplate().getDataSource());
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (args != null && args.length != 0 ? ArrayUtils.toString(args) : ""));
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public static int execute(String sql, Object... args) {
        return (Integer) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                Connection con = null;
                boolean var6 = false;

                Integer var12;
                try {
                    con = DataSourceUtils.getConnection(DBHelper.getJdbcTemplate().getDataSource());
                    pstmt = con.prepareStatement(sql);
                    if (args != null) {
                        for (int i = 0; i < args.length; ++i) {
                            if (args[i] != null && args[i].getClass().isArray()) {
                                if (Array.getLength(args[i]) > 1) {
                                    String key = (String) Array.get(args[i], 0);
                                    Object value = Array.get(args[i], 1);
                                    if ("clob".equalsIgnoreCase(key)) {
                                        String tmpValuex = (String) value;
                                        DBHelper.getLobHandler().getLobCreator().setClobAsCharacterStream(pstmt, i + 1, new StringReader(tmpValuex), tmpValuex.length());
                                    } else if ("blob".equalsIgnoreCase(key)) {
                                        byte[] tmpValue = (byte[]) value;
                                        DBHelper.getLobHandler().getLobCreator().setBlobAsBytes(pstmt, i + 1, tmpValue);
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
                    DataSourceUtils.releaseConnection(con, DBHelper.getJdbcTemplate().getDataSource());
                }

                return var12;
            }
        }, (Class) null, args);
    }

    public static int update(String sql, Object... args) {
        return execute(sql, args);
    }

    public static int[] batch(String... sql) {
        long t1 = System.currentTimeMillis();

        int[] var4;
        try {
            var4 = getJdbcTemplate().batchUpdate(sql);
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql);
        }

        return var4;
    }

    public static <T> int[] batch(String sql, List<T> paramlist) {
        long t1 = System.currentTimeMillis();

        int[] var5;
        try {
            var5 = getNamedParameterJdbcTemplate().batchUpdate(sql, paramBeanMapper(paramlist));
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql);
        }

        return var5;
    }

    public static int update(String sql, Map<String, ?> paramMap) {
        long t1 = System.currentTimeMillis();

        int var5;
        try {
            var5 = getNamedParameterJdbcTemplate().update(sql, paramMap);
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (paramMap == null ? "" : ArrayUtils.toString(paramMap)));
        }

        return var5;
    }

    public static <T> int update(String sql, T paramBean) {
        long t1 = System.currentTimeMillis();

        int var5;
        try {
            var5 = getNamedParameterJdbcTemplate().update(sql, paramBeanMapper(paramBean));
        } finally {
            long t2 = System.currentTimeMillis();
            logger.debug((double) (t2 - t1) / 1000.0D + "s " + sql + " " + (paramBean == null ? "" : ArrayUtils.toString(paramBean)));
        }

        return var5;
    }

    public static <T> List<T> queryForList(String sql, final Class<T> clz, Object... objects) {
        return (List) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                return DBHelper.getJdbcTemplate().query(sql, BeanPropertyRowMapper.newInstance(clz), args);
            }
        }, clz, objects);
    }

    public static <T> T queryForBean(String sql, Class<T> clz, Object... objects) {
        List<T> list = queryForList(sql, clz, objects);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public static List<Map<String, Object>> queryForList(String sql, Object... args) {
        return (List) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                return DBHelper.getJdbcTemplate().queryForList(sql, args);
            }
        }, (Class) null, args);
    }

    public static Map<String, Object> queryForMap(String sql, Object... args) {
        List<Map<String, Object>> list = queryForList(sql, args);
        return list != null && list.size() > 0 ? (Map) list.get(0) : null;
    }

    public static Map<String, Object> queryForMap(String sql, Map args) {
        List<Map<String, Object>> list = queryForList(sql, args);
        return list != null && list.size() > 0 ? (Map) list.get(0) : null;
    }

    public static <T> T queryForScalar(String sql, final Class<T> clz, Object... args) {
        return (T) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                Object object = null;
                try {
                    object = DBHelper.getJdbcTemplate().queryForObject(sql,clz,args);
                } catch (EmptyResultDataAccessException e) {
                  //  e.printStackTrace(); // 可以选择打印信息
                    return null;
                }
                return object;
            }
        }, (Class) null, args);
    }

    public static Page<?> queryForPage(String sql, int pageNo, int pageSize, Object... args) {
        return queryForPage(sql, pageNo, pageSize, 0, args);
    }

    public static Page<?> queryForPage(String sql, final int pageNo, final int pageSize, final int totalCount, Object... args) {
        return (Page) execSQL(sql, new SQLCallback() {
            public Object doInCallback(String sql, Object... args) throws SQLException {
                Integer total = totalCount;
                Dialect dialect = DBHelper.getDialect();
                String cnt_sql = dialect.getCountString(sql);
                if (totalCount <= 0) {
                    total = (Integer) DBHelper.getJdbcTemplate().queryForObject(cnt_sql, Integer.class, args);
                }

                if (total < 1) {
                    return new Page();
                } else {
                    int startIndex = Page.getStartOfPage(pageNo, pageSize);
                    String page_sql = dialect.getLimitString(sql, startIndex, startIndex + pageSize);
                    DBHelper.logger.debug("PagedSQL: " + page_sql);
                    List<?> resultList = DBHelper.getJdbcTemplate().queryForList(page_sql, args);
                    return new Page(startIndex, total, pageSize, resultList);
                }
            }
        }, (Class) null, args);
    }

    public static ProcedureResult executeProcedure(String sql, Object... paramArgs) {
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
        }, paramArgs);
    }


    public static <T> int save(T model) {
        try{
            Class<?> modelClass = model.getClass();
            String table =  modelClass.getAnnotation(Table.class).value();
            String id =  modelClass.getAnnotation(ID.class).value();
            if(StringUtils.isBlank(table)){
                throw new RuntimeException(model.getClass().getName() + " "+ Table.class.getName() +" 为空 ");
            }
            String fieldName = " ";
            String params = " ";

            List args = new ArrayList();
            Field[] fields = model.getClass().getDeclaredFields();
            for(Field field : fields){

                if(field.getDeclaredAnnotationsByType(NotField.class).length == 0){
                    Column column = field.getAnnotation(Column.class);

                    Object   value = modelClass.getMethod("get" + StringHelper.firstCharToUpperCase(field.getName())).invoke(model);
                    if(StringHelper.notNull(value)){
                        String key = "";
                        if(column != null) {
                            key = column.value();
                        }else{
                            key = field.getName();

                        }
                        fieldName += ("," + key);
                        if(key.equalsIgnoreCase(id)){
                            params += ","+value;
                        }else{
                            params += ",?";
                            args.add(value);
                        }


                    }
                }


            }
            return update("insert into "+ table + " ("+fieldName.replaceFirst(",","")+") values(" + params.replaceFirst(",","")+")",args.toArray());
        }catch (NoSuchMethodException ne){
            ne.printStackTrace();
            return 0;
        }catch (InvocationTargetException ne){
            ne.printStackTrace();
            return 0;
        }catch (IllegalAccessException ne){
            ne.printStackTrace();
            return 0;
        }
    }


    public static <T> int modify(T model) {
        try{
            Class<?> modelClass = model.getClass();
            String table =  modelClass.getAnnotation(Table.class).value();
            String id =  modelClass.getAnnotation(ID.class).value();

            if(StringHelper.isBlank(table)){
                throw new RuntimeException(model.getClass().getName() + " "+ Table.class.getName() +" 为空 ");
            }
            if(StringHelper.isBlank(id)){
                throw new RuntimeException(model.getClass().getName() + " "+ ID.class.getName() +" 为空 ");
            }
            id = id.toLowerCase();
            Object idValue = modelClass.getMethod("get"+StringHelper.firstCharToUpperCase(id)).invoke(model);
            if(!StringHelper.notNull(idValue)){
                throw new RuntimeException(model.getClass().getName() + " 主键为空 ");
            }
            String set = " ";

            List args = new ArrayList();
            Field[] fields = model.getClass().getDeclaredFields();
            for(Field field : fields){
                if(field.getDeclaredAnnotationsByType(NotField.class).length == 0&&!field.getName().equals(id)){
                    Object value = modelClass.getMethod("get"+StringHelper.firstCharToUpperCase(field.getName())).invoke(model);
                    if(StringHelper.notNull(value)){
                        set += (",t."+field.getName()+"=?");
                        args.add(value);
                    }
                }
            }
            args.add(idValue);
            return update("update "+ table + " t  set "+set.replaceFirst(",","")+" where t."+id+"=?",args.toArray());
        }catch (NoSuchMethodException ne){
            ne.printStackTrace();
            return 0;
        }catch (InvocationTargetException ne){
            ne.printStackTrace();
            return 0;
        }catch (IllegalAccessException ne){
            ne.printStackTrace();
            return 0;
        }
    }


    public static <T> int delete(T model, Object... objects) {
        Class<?> clz = model.getClass();
        String table =  clz.getAnnotation(Table.class).value();
        String id =  clz.getAnnotation(ID.class).value();
        if(StringHelper.isBlank(table)){
            throw new RuntimeException(clz.getName() + " "+ Table.class.getName() +" 为空 ");
        }
        if(StringHelper.isBlank(id)){
            throw new RuntimeException(clz.getName() + " "+ ID.class.getName() +" 为空 ");
        }
        return update(" delete from "+table+" t where t."+id +" = ?",objects);
    }

    public static <T> int delete(String tableName, String primaryKey ,String value) {
        System.out.println(" delete from "+tableName+" t where t."+primaryKey +" = ?");
        return execute(" delete from "+tableName+" t where t."+primaryKey +" = ?",new Object[]{value});
    }





    public static void main(String[] args) {


        List<Map<String, Object>> l_m = DBHelper.queryForList("select * from user ");

      //  DBHelper.save("ss_role","id",new HashMap<>());
        logger.error("123");

    }
}
