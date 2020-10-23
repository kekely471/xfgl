package com.tonbu.framework.dao.dialect;


import org.apache.commons.lang3.StringUtils;

public class DB2Dialect extends Dialect {
    public DB2Dialect() {
    }

    public boolean supportsLimitOffset() {
        return true;
    }

    private int getLastIndexOfOrderBy(String sql) {
        return sql.toLowerCase().lastIndexOf("order by ");
    }

    private boolean hasDistinct(String sql) {
        return sql.toLowerCase().indexOf("select distinct") >= 0;
    }

    private String getRowNumber(String sql) {
        StringBuffer rownumber = (new StringBuffer(50)).append("rownumber() over(");
        int orderByIndex = sql.toLowerCase().indexOf("order by");
        if (orderByIndex > 0) {
            String orderBy = sql.substring(orderByIndex);
            if (this.hasDistinct(sql)) {
                StringBuffer orderByBuffer = new StringBuffer(" order by ");
                String oldorderBy = StringUtils.replace(orderBy, "order by ", "");
                String[] cols = StringUtils.split(oldorderBy, ",");
                String[] var11 = cols;
                int first1 = cols.length;

                int dotIx;
                String col;
                for(dotIx = 0; dotIx < first1; ++dotIx) {
                    col = var11[dotIx];
                    String newcol = col;
                    col = StringUtils.substringBefore(col, ".");
                    if (col != null) {
                        newcol = StringUtils.replace(col, col + ".", "row_.");
                    }

                    orderByBuffer.append(newcol).append(",");
                }

                orderBy = orderByBuffer.substring(0, orderByBuffer.length() - 1).toString();
                int fromIndex = 0;

                while(true) {
                    dotIx = orderBy.indexOf(".", fromIndex);
                    if (dotIx == -1) {
                        rownumber.append(orderBy);
                        break;
                    }

                    first1 = orderBy.indexOf(" ", dotIx);
                    int first2 = orderBy.indexOf(",", dotIx);
                    int endIndex = this.getEndIndex(first1, first2);
                    col = null;
                    if (endIndex == -1) {
                        col = orderBy.substring(dotIx + 1);
                    } else {
                        col = orderBy.substring(dotIx + 1, endIndex);
                    }

                    String beforeStr = "." + col + " as ";
                    int pos = sql.indexOf(beforeStr);
                    if (pos > -1) {
                        pos += beforeStr.length();
                        first1 = sql.indexOf(" ", pos);
                        first2 = sql.indexOf(",", pos);
                        endIndex = this.getEndIndex(first1, first2);
                        String colAlias = null;
                        if (endIndex == -1) {
                            colAlias = sql.substring(pos);
                        } else {
                            colAlias = sql.substring(pos, endIndex);
                        }

                        orderBy = orderBy.replaceAll(col, colAlias);
                    }

                    fromIndex = dotIx + 1;
                }
            } else {
                rownumber.append(orderBy);
            }
        }

        rownumber.append(") as rownumber_,");
        return rownumber.toString();
    }

    private int getEndIndex(int first1, int first2) {
        if (first1 == -1 && first2 == -1) {
            return -1;
        } else {
            return first1 > -1 && first2 > -1 ? Math.min(first1, first2) : first1 + first2 + 1;
        }
    }

    public String getLimitString(String sql, int offset, int limit) {
        int startOfSelect = sql.toLowerCase().indexOf("select");
        StringBuffer pagingSelect = (new StringBuffer(sql.length() + 100)).append(sql.substring(0, startOfSelect)).append("select * from ( select ").append(this.getRowNumber(sql));
        int orderByIndex = sql.toLowerCase().indexOf("order by");
        if (this.hasDistinct(sql)) {
            pagingSelect.append(" row_.* from ( ");
            if (orderByIndex > 0) {
                pagingSelect.append(sql.substring(startOfSelect, orderByIndex));
            } else {
                pagingSelect.append(sql.substring(startOfSelect));
            }

            pagingSelect.append(" ) as row_");
        } else if (orderByIndex > 0) {
            pagingSelect.append(sql.substring(startOfSelect + 6, orderByIndex));
        } else {
            pagingSelect.append(sql.substring(startOfSelect + 6));
        }

        pagingSelect.append(" fetch first ").append(limit).append(" rows only) as temp_ where rownumber_ ");
        if (offset > 0) {
            pagingSelect.append("between " + (offset + 1) + " and " + limit);
        } else {
            pagingSelect.append("<= " + limit);
        }

        return pagingSelect.toString();
    }

    public String getCountString(String sql) {
        int lastIndexOfOrderBy = this.getLastIndexOfOrderBy(sql);
        int indexOfFrom = sql.toLowerCase().indexOf("from");
        String selectFromTableAndWhere = "";
        if (lastIndexOfOrderBy > -1) {
            selectFromTableAndWhere = sql.substring(indexOfFrom, lastIndexOfOrderBy);
        } else {
            selectFromTableAndWhere = sql.substring(indexOfFrom);
        }

        StringBuffer sqlbuff = new StringBuffer(sql.length());
        sqlbuff.append("select count(1) ").append(selectFromTableAndWhere);
        return sqlbuff.toString();
    }

    public static void main(String[] args) {
        Dialect dialect = new DB2Dialect();
        System.out.println(dialect.getLimitString("select distinct a.zh, a.zhjgm,a.userid,hm,khrq ,e.jgjc as zhjgname ,' ' as zrlname from bcas_zhdyb_ck order by a.userid,b.zh", 1, 5));
    }
}
