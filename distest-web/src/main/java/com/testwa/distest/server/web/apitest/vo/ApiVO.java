package com.testwa.distest.server.web.apitest.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


/**
 * @author wen
 * @create 2018-12-24 15:09
 */
@Data
public class ApiVO {
    private Long id;
    @JsonIgnore
    private Long categoryId;
    private String ApiName;
    private String method;
    @JsonIgnore
    private String url;
}
