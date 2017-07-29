package com.testwa.distest.server.mvc.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by wen on 29/07/2017.
 */
@ApiModel(value = "DelParams",                           // 模型名称
        description = "删除接口所用的参数"
)
public class DelParams {

    @ApiModelProperty(value = "id", example = "['0001','0002']")
    public List<String> ids;

}
