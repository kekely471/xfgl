package com.tonbu.framework.util;


import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class JdbcUtils {
    public JdbcUtils() {
    }

    public static List<Map<String, Object>> convertResultSetToMapList(ResultSet rs, Boolean toLowerCase) {
        List<Map<String, Object>> resultListMap = new ArrayList();
        if (rs == null) {
            return resultListMap;
        } else {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                while(rs.next()) {
                    Map<String, Object> rowMap = new LinkedHashMap();

                    for(int i = 1; i <= columnCount; ++i) {
                        Object colValue = null;
                        String colName = rsmd.getColumnLabel(i);
                        if (toLowerCase) {
                            colName = colName.toLowerCase();
                        }

                        if (rsmd.getColumnType(i) == 2005) {
                            Clob clob = rs.getClob(i);
                            if (clob != null && (int)clob.length() > 0) {
                                colValue = clob.getSubString(1L, (int)clob.length());
                            }
                        } else if (rsmd.getColumnType(i) == 2004) {
                            colValue = rs.getBytes(i);
                        } else {
                            colValue = rs.getObject(i);
                        }

                        rowMap.put(colName, colValue);
                    }

                    resultListMap.add(rowMap);
                }
            } catch (SQLException var18) {
                var18.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException var17) {
                        rs = null;
                    }
                }

            }

            return resultListMap;
        }
    }

    public static void bindStatementInput(CallableStatement cs, int indexed, Object value) throws SQLException {
        if (value instanceof String) {
            cs.setString(indexed, (String)value);
        } else if (value instanceof Integer) {
            cs.setInt(indexed, (Integer)value);
        } else if (value instanceof Double) {
            cs.setDouble(indexed, (Double)value);
        } else if (value instanceof Float) {
            cs.setFloat(indexed, (Float)value);
        } else if (value instanceof Long) {
            cs.setLong(indexed, (Long)value);
        } else if (value instanceof Boolean) {
            cs.setBoolean(indexed, (Boolean)value);
        } else if (value instanceof Date) {
            cs.setDate(indexed, new java.sql.Date(((Date)value).getTime()));
        } else if (value instanceof java.sql.Date) {
            cs.setDate(indexed, (java.sql.Date)value);
        } else if (value instanceof BigDecimal) {
            cs.setBigDecimal(indexed, (BigDecimal)value);
        } else if (value instanceof Timestamp) {
            cs.setTimestamp(indexed, (Timestamp)value);
        } else if (value instanceof Time) {
            cs.setTime(indexed, (Time)value);
        } else if (value instanceof Blob) {
            cs.setBlob(indexed, (Blob)value);
        } else if (value instanceof Clob) {
            cs.setClob(indexed, (Clob)value);
        } else {
            cs.setObject(indexed, value);
        }

    }

    public static void bindStatementOutput(CallableStatement cs, int indexed, String dataType) throws SQLException {
        dataType = StringUtils.lowerCase(dataType);
        if (!StringUtils.equals(dataType, "string") && !StringUtils.equals(dataType, "char")) {
            if (!StringUtils.equals(dataType, "int") && !StringUtils.equals(dataType, "integer")) {
                if (StringUtils.equals(dataType, "date")) {
                    cs.registerOutParameter(indexed, 91);
                } else if (StringUtils.equals(dataType, "decimal")) {
                    cs.registerOutParameter(indexed, 3);
                } else if (StringUtils.equals(dataType, "float")) {
                    cs.registerOutParameter(indexed, 6);
                } else if (StringUtils.equals(dataType, "double")) {
                    cs.registerOutParameter(indexed, 8);
                } else if (StringUtils.equals(dataType, "cursor")) {
                    cs.registerOutParameter(indexed, -10);
                } else {
                    cs.registerOutParameter(indexed, 1);
                }
            } else {
                cs.registerOutParameter(indexed, 4);
            }
        } else {
            cs.registerOutParameter(indexed, 1);
        }

    }

    public static Object convertType(CallableStatement cs, int indexed, String dataType) {
        dataType = StringUtils.lowerCase(dataType);
        Object dataValue = null;

        try {
            dataValue = cs.getObject(indexed);
        } catch (SQLException var5) {

        }

        if (dataValue == null) {
            return null;
        } else if (!StringUtils.equals(dataType, "string") && !StringUtils.equals(dataType, "char")) {
            if (!StringUtils.equals(dataType, "int") && !StringUtils.equals(dataType, "integer")) {
                if (StringUtils.equals(dataType, "date")) {
                    return dataValue;
                } else if (StringUtils.equals(dataType, "decimal")) {
                    return new BigDecimal(StringUtils.defaultIfEmpty((String)dataValue, "0"));
                } else if (StringUtils.equals(dataType, "float")) {
                    return new Float(StringUtils.defaultIfEmpty((String)dataValue, "0"));
                } else if (StringUtils.equals(dataType, "double")) {
                    return new Double(StringUtils.defaultIfEmpty((String)dataValue, "0"));
                } else {
                    return StringUtils.equals(dataType, "cursor") ? convertResultSetToMapList((ResultSet)dataValue, false) : dataValue;
                }
            } else {
                return (Integer)dataValue;
            }
        } else {
            return dataValue;
        }
    }

    public static void close(Connection conn, ResultSet rs, Statement stmt) {
        close(rs);
        close(stmt);
        close(conn);
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException var2) {
                rs = null;
            }
        }

    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException var2) {
                conn = null;
            }
        }

    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException var2) {
                stmt = null;
            }
        }

    }
}
