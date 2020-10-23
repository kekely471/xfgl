package com.tonbu.framework.data;


import com.tonbu.framework.Constants;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.framework.util.XlsUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class Limit {
    public static final String PAGE_START = "page";
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_SORT = "sort";
    public static final String PAGE_ORDER = "order";
    public static final String PAGE_COUNT = "count";
    public static final String EXPORT_FLAG = "export.xls";
    public static final String EXPORT_COLS = "export.cols";
    public static final String EXPORT_RANGE = "export.range";
    public static final String RESULT_START = "startresult";
    public static final String RESULT_END = "endresult";
    public static final String RESULT_TOTAL = "totalresult";
    private Integer start;
    private Integer size;
    private String sort;
    private String order;
    private Integer count;
    private Integer startresult;
    private Integer endresult;
    private Integer totalresult;
    private HttpServletRequest request;

    public Limit() {
    }

    public Limit(HttpServletRequest request) {
        this.request = request;
        this.fetchInfo();
    }

    public Limit(Map<String, String> param) {
        if (param != null) {
            String limit = param.get("limit");
            if (StringUtils.isNotEmpty(limit)) {
                this.size = Integer.parseInt(limit);
            } else {
                this.size = Integer.parseInt(param.get("rows"));
            }
            this.start = Integer.parseInt((String) param.get("page"));
            if (this.size == 0) {
                this.size = Integer.parseInt((String) param.get("rows"));
            }
            if (param.get("sort") != null) {
                this.sort = ((String) param.get("sort")).toString();
            }
            if (param.get("field") != null) {
                this.sort = ((String) param.get("field")).toString();
            }
            if (param.get("order") != null) {
                this.order = ((String) param.get("order")).toString();
            }

            this.count = Integer.parseInt((String) param.get("count"));
            this.startresult = Integer.parseInt((String) param.get("startresult"));
            this.endresult = Integer.parseInt((String) param.get("endresult"));
            this.totalresult = Integer.parseInt((String) param.get("totalresult"));
        }

    }

    protected void fetchInfo() {
        this.fetchLimitInfo();
        this.fetchExportInfo();
        this.genLimitInfo();
    }

    protected void fetchLimitInfo() {
        if (this.request != null) {
            this.start = RequestUtils.getParam(this.request, "page", 1);
            this.size = RequestUtils.getParam(this.request, "limit", Constants.DEFAULT_PAGE_SIZE);
            this.count = RequestUtils.getParam(this.request, "count", 0);
        }

    }

    protected void fetchExportInfo() {
        if (this.request != null) {
            Integer range = RequestUtils.getParam(this.request, "export.range", 0);
            if (this.isExport() && range != 0) {
                this.start = 1;
                if (this.count > 0) {
                    this.size = this.count;
                } else {
                    this.size = 10000000;
                }
            }
        }

    }

    protected void genLimitInfo() {
        this.startresult = (this.start - 1) * this.size;
        this.endresult = this.startresult + this.size;
        if (this.startresult < 0) {
            this.startresult = 0;
        }

        if (this.endresult < this.startresult) {
            this.endresult = this.startresult;
        }

        this.totalresult = this.count;
    }

    public String getSort() {
        return this.sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Integer getStart() {
        return this.start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getStartresult() {
        return this.startresult;
    }

    public void setStartresult(Integer startresult) {
        this.startresult = startresult;
    }

    public Integer getEndresult() {
        return this.endresult;
    }

    public void setEndresult(Integer endresult) {
        this.endresult = endresult;
    }

    public Integer getTotalresult() {
        return this.totalresult;
    }

    public void setTotalresult(Integer totalresult) {
        this.totalresult = totalresult;
    }

    public Boolean isExport() {
        return BooleanUtils.toBooleanObject(RequestUtils.getParam(this.request, "export.xls", "false"));
    }

    public Boolean isNotExport() {
        return !this.isExport();
    }

    public List<Map<String, String>> getExportColumns() {
        String col = RequestUtils.getParam(this.request, "export.cols", "");
        String[] cols = StringUtils.split(col, ",");
        List<Map<String, String>> list = new ArrayList();

        for (int i = 0; i < cols.length; ++i) {
            String[] onecol = cols[i].split(":");
            Map<String, String> map = new HashMap();
            map.put("name", onecol[0]);
            map.put("display", onecol[1]);
            if (onecol.length > 2) {
                map.put("width", onecol[2]);
            }

            list.add(map);
        }

        return list;
    }

    public void toExcel(List<Map<String, Object>> dataList, OutputStream out) {
        XlsUtils.gridExportToExcel(this.getExportColumns(), dataList, out);
    }

    public String toExcel(List<Map<String, Object>> dataList) {
        return XlsUtils.gridExportToExcel(this.getExportColumns(), dataList);
    }

    public String toExcelModel(List<Map<String, Object>> dataList) {
        return this.toExcel(dataList);
    }
}
