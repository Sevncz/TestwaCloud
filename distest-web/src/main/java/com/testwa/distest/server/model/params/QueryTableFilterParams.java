package com.testwa.distest.server.model.params;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/12/2016.
 */
@ApiModel(value = "QueryTableFilterParams",                           // 模型名称
        description = "table query params"
)
public class QueryTableFilterParams {
    @ApiModelProperty(value = "page first", example = "1")
    public Integer first;
    @ApiModelProperty(value = "page rows", example = "15")
    public Integer rows;
    @ApiModelProperty(value = "page sort field", example = "")
    public String sortField;
    @ApiModelProperty(value = "page sort order", example = "")
    public String sortOrder;
    @ApiModelProperty(value = "内容 contains(0, \"contains\"), in(1, \"in\"), is(2, \"is\"), startwith(3, \"startwith\"), endwith(4, \"endwith\");")
    public List<Map<String, Object>> filters = new ArrayList<>();

    @Override
    public String toString() {
        return "QueryTableFilterParams{" +
                "first=" + first +
                ", rows=" + rows +
                ", sortField='" + sortField + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", filters=" + filters +
                '}';
    }
}
