package com.testwa.distest.server.mvc.beans;

import com.google.common.collect.Lists;
import com.testwa.distest.server.mvc.vo.DeviceOwnerTableVO;

import java.util.List;

/**
 * Created by wen on 15/07/2017.
 */
public class PageResult<T> {

    private List<T> pages = Lists.newArrayList();

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
