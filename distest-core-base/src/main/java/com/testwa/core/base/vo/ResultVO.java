package com.testwa.core.base.vo;

import lombok.Data;

/**
 * Created by wen on 2016/11/19.
 */
@Data
public class ResultVO<T> {

    private Integer code;
    private String type;
    private String message;
    private String url;
    private T data;
}
