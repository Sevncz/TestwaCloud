package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * api 分类
 *
 * @author wen
 * @create 2018-12-17 17:30
 */
@Data
@Table(name="dis_api_category")
public class ApiCategory extends ProjectBase{

    @Column(name = "category_name")
    private String categoryName;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(name = "authorization")
    private String authorization;
    @Column(name = "pre_script")
    private String preScript;
    @Column(name = "script")
    private String script;
    @Column(name = "description")
    private String description;
    // 节点路径
    @Column(name = "path")
    private String path;
    // 节点层级
    @Column(name = "level")
    private Integer level;
    // 节点排序
    @Column(name = "seq")
    private Integer seq;



}
