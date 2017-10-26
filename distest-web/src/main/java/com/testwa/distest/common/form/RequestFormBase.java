package com.testwa.distest.common.form;


import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ApiModel(value = "RequestFormBase",
        description = "请求参数"
)
@ToString
@Data
public abstract class RequestFormBase implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7484305986335855048L;

}