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
        description = "table query beans"
)
public class PageQuery {
    @ApiModelProperty(value = "当前页码", example = "1")
    public Integer first;
    @ApiModelProperty(value = "每页展示数量", example = "15")
    public Integer rows;
    @ApiModelProperty(value = "排序字段", example = "id")
    public String sortField;
    @ApiModelProperty(value = "排序", example = "0")
    public String sortOrder;
    @ApiModelProperty(value = "内容 contains(0, \"contains\"), in(1, \"in\"), is(2, \"is\"), startwith(3, \"startwith\"), endwith(4, \"endwith\");")
    public List<Map<String, Object>> filters = new ArrayList<>();

}
