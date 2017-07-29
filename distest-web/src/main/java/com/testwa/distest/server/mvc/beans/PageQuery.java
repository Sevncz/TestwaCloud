package com.testwa.distest.server.mvc.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/12/2016.
 */
@ApiModel(value = "PageQuery",                           // 模型名称
        description = "table分页参数"
)
public class PageQuery {
    @ApiModelProperty(value = "当前页码", example = "1")
    public Integer page;
    @ApiModelProperty(value = "每页展示数量", example = "15")
    public Integer limit;
    @ApiModelProperty(value = "排序字段", example = "id")
    public String sortField;
    @ApiModelProperty(value = "排序", example = "desc")
    public String sortOrder;
    @ApiModelProperty(value = "查询", example = "")
    public Map<String, Object> query;
}
