
package com.tonbu.framework.data;

import com.tonbu.framework.dao.support.Page;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultEntity implements Serializable {
    private Integer code = 0;
    private String msg;
    private DataSet dataset;

    public ResultEntity() {
    }

    public static ResultEntity newInstance(){
        return new ResultEntity();
    }

    public ResultEntity success(){
        this.code = Integer.valueOf(1);
        this.msg = "操作成功";
        return this;
    }

    public ResultEntity error(String code){
        this.code = Integer.valueOf(code);
        this.msg = "操作失败";
        return this;
    }

    public ResultEntity error(String code,String msg){
        this.code = Integer.valueOf(code);
        this.msg = msg;
        return this;
    }

    public ResultEntity toAjax(int rows){
        return rows > 0 ? success() : error("-1");
    }

    public ResultEntity toAjax(boolean result){
        return result ? success() : error("-1");
    }

    public ResultEntity(Integer code) {
        this.code = code;
    }

    public ResultEntity(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultEntity(Integer code, String msg, DataSet dataset) {
        this.code = code;
        this.msg = msg;
        this.dataset = dataset;
    }

    public ResultEntity(Integer code, String msg, Page<?> page) {
        this.code = code;
        this.msg = msg;
        this.dataset = new DataSet(page.getRows(), page.getPageCount(), page.getCurrentPageNo(), page.getPageSize(), page.getPageCount());
    }

    public ResultEntity(Integer code, String msg, List<?> list) {
        this.code = code;
        this.msg = msg;
        this.dataset = new DataSet(list);
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCodeMsg(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Object get(String key) {
        if (this.dataset != null) {
            List list = this.dataset.getRows();
            if (list != null && list.size() > 0) {
                Map<String, Object> map = (Map)list.get(0);
                if (map != null) {
                    return map.get(key);
                }
            }
        }

        return null;
    }

    public DataSet getDataset() {
        return this.getDataset(true);
    }

    public DataSet getDataset(boolean isCreate) {
        if (this.dataset == null && isCreate) {
            this.dataset = new DataSet();
        }

        return this.dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
        this.dataset.setTotal(dataset.getRows().size());
    }

    public void setDataset(List<?> rows) {
        this.dataset = this.getDataset(true);
        this.dataset.setRows(rows);
        this.dataset.setTotal(rows.size());
    }

    public Object setRow(Map row) {
        List data = new ArrayList<>();
        data.add(row);
        this.dataset = this.getDataset(true);
        this.dataset.setRows(data);
        this.code = 1;
        return this;
    }

    public Object setRows(List<?> rows) {
        this.dataset = this.getDataset(true);
        this.dataset.setRows(rows);
        this.dataset.setTotal(rows.size());
        this.code = 1;
        return this;
    }

    public void setDataset(Page<?> page) {
        this.dataset = this.getDataset(true);
        this.dataset.setRows(page.getRows());
        this.dataset.setTotal(page.getTotalCount());
        this.dataset.setPage(page.getCurrentPageNo());
        this.dataset.setLimit(page.getPageSize());
        this.dataset.setPages(page.getPageCount());
    }

    public static class DataSet {
        private int total;
        private int page;
        private int limit;
        private List<?> rows = new ArrayList();
        private int pages;

        public DataSet() {
        }

        public DataSet(List rows, Integer total, Integer page, Integer limit, Integer pages) {
            this.rows = rows;
            this.page = page;
            this.limit = limit;
            this.total = total;
            this.pages = pages;
        }

        public DataSet(List rows) {
            this.rows = rows;
            this.page = 1;
            this.total = this.rows != null ? this.rows.size() : 0;
            this.limit = this.total;
            this.pages = this.total;
        }

        public int getTotal() {
            return this.total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getLimit() {
            return this.limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getPages() {
            return this.pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getPage() {
            return this.page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public List getRows() {
            return this.rows;
        }

        public void setRows(List rows) {
            this.rows = rows;
        }
    }
}
