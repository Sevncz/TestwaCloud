package com.testwa.distest.server.web.apitest.vo;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wen
 * @create 2018-12-24 15:09
 */
@Data
public class ApiDetailVO {
    private Long id;
    private String apiName;
    @JsonIgnore
    private Long categoryId;
    private String method;
    private String url;
    private String param;
    private String authorization;
    private String header;
    private String body;
    private String preScript;
    private String script;
    private String description;
    private Date createTime;

    public Map getAuthorization() {
        if(StringUtils.isEmpty(this.authorization)){
            return new HashMap<>();
        }
        return JSON.parseObject(this.authorization, Map.class);
    }

    public Map getHeader() {
        if(StringUtils.isEmpty(this.header)){
            return new HashMap<>();
        }
        return JSON.parseObject(this.header, Map.class);
    }

    public Map getBody() {
        if(StringUtils.isEmpty(this.body)){
            return new HashMap<>();
        }
        return JSON.parseObject(this.body, Map.class);
    }

    public Map getParam() {
        if(StringUtils.isEmpty(this.param)){
            return new HashMap<>();
        }
        return JSON.parseObject(this.param, Map.class);
    }
}
