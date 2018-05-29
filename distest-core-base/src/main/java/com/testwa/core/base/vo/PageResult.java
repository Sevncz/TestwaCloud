package com.testwa.core.base.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 15/07/2017.
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> pages = new ArrayList<>();
    private long total = 0;
}
