package com.tonbu.framework.dao.support;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page<T> implements Serializable {
    private static final long serialVersionUID = -2716101057846073321L;
    private static final Integer DEFAULT_PAGE_SIZE = 50;
    private int start;
    private int pageSize;
    private int pageNo;
    private int total;
    private List<T> rows;
    private List<T> footer;

    public Page() {
        this(0, 0, DEFAULT_PAGE_SIZE, new ArrayList());
    }

    public Page(int start, int total, int pageSize, List<T> rows) {
        this.start = 0;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.pageNo = 1;
        this.start = start;
        this.total = total;
        this.pageSize = pageSize;
        this.pageNo = this.getCurrentPageNo();
        this.rows = rows;
        this.validator();
    }

    public int getCurrentPageNo() {
        if (this.pageSize == 0) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        }

        return this.start / this.pageSize + 1;
    }

    public List<T> getRows() {
        return this.rows;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getTotalCount() {
        return this.total;
    }

    public int getTotalPageCount() {
        return this.total % this.pageSize == 0 ? this.total / this.pageSize : this.total / this.pageSize + 1;
    }

    public boolean hasNextPage() {
        return this.getCurrentPageNo() < this.getTotalPageCount();
    }

    public boolean hasPreviousPage() {
        return this.getCurrentPageNo() > 1;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    private void validator() {
        if (this.start < 0) {
            this.start = 0;
        }

        if (this.total < 0) {
            this.total = 0;
        }

        if (this.pageSize <= 0) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        }

        if (this.pageNo <= 0) {
            this.pageNo = 1;
        }

        this.pageNo = this.getCurrentPageNo();
    }

    public boolean isHasNextPage() {
        return this.hasNextPage();
    }

    public boolean isHasPreviousPage() {
        return this.hasPreviousPage();
    }

    public int getPageCount() {
        return this.getTotalPageCount();
    }

    public static int getStartOfPage(int pageNo) {
        return getStartOfPage(pageNo, DEFAULT_PAGE_SIZE);
    }

    public static int getStartOfPage(int pageNo, int pageSize) {
        if (pageNo <= 0) {
            pageNo = 1;
        }

        if (pageSize == 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return (pageNo - 1) * pageSize;
    }

    public List<T> getFooter() {
        return this.footer;
    }

    public void setFooter(List<T> footer) {
        this.footer = footer;
    }
}
