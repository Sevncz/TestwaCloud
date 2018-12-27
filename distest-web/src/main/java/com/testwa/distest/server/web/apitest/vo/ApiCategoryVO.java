package com.testwa.distest.server.web.apitest.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-24 15:09
 */
@Data
public class ApiCategoryVO{
    private Long id;
    private String parentId;
    private String categoryName;
    private Integer seq;
    private String authorization;
    private String preScript;
    private String script;
    private String description;
    private List<ApiCategoryVO> children = new ArrayList<>();

    public void add(ApiCategoryVO item) {
        this.children.add(item);
    }

    public void remove(ApiCategoryVO item) {
        this.children.remove(item);
    }


}
