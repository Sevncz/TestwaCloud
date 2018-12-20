package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

/**
 * api 测试的配置
 *
 * @author wen
 * @create 2018-12-17 16:48
 */
@Data
@Table(name= "dis_api")
public class Api extends ProjectBase{

    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "method")
    private String method;
    @Column(name = "url")
    private String url;
    @Column(name = "param")
    private String param;
    @Column(name = "authorization")
    private String authorization;
    @Column(name = "header")
    private String header;
    @Column(name = "body")
    private String body;
    @Column(name = "pre_script")
    private String preScript;
    @Column(name = "script")
    private String script;
    @Column(name = "description")
    private String description;


}
