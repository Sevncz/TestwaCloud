package com.testwa.core.base.vo;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 15/07/2017.
 */
public class PageResult<T> {

    private List<T> pages = new ArrayList<>();

    private long total = 0;

    public PageResult(List<T> pages, long total) {
        this.pages = pages;
        this.total = total;
    }

    public List<T> getPages() {
        return pages;
    }

    public void setPages(List<T> pages) {
        this.pages = pages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
