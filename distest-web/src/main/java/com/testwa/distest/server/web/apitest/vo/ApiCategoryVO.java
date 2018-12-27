package com.testwa.distest.server.web.apitest.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wen
 * @create 2018-12-24 15:09
 */
@Data
public class ApiCategoryVO{
    private Long id;
    private String categoryName;
    private Integer seq;
    private String authorization;
    private String preScript;
    private String script;
    private String description;
    private List<ApiCategoryVO> folders = new ArrayList<>();
    private List<ApiVO> items = new ArrayList<>();

    public void add(ApiCategoryVO item) {
        this.folders.add(item);
    }

    public void remove(ApiCategoryVO item) {
        this.folders.remove(item);
    }

    public void addItems(List<ApiVO> apiVOList) {
        if(apiVOList != null) {
            items.addAll(apiVOList);
        }
    }

    public Map getAuthorization() {
        if(StringUtils.isEmpty(this.authorization)){
            return new HashMap<>();
        }
        return JSON.parseObject(this.authorization, Map.class);
    }
}
