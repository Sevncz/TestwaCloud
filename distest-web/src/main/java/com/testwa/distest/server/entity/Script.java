package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.common.enums.DB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by wen on 16/9/1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="script")
public class Script extends ProjectBaseEntity {

    @Column(name = "scriptName")
    private String scriptName;
    @Column(name = "aliasName")
    private String aliasName;
    @Column(name = "appPackage")
    private String appPackage;
    @Column(name = "size")
    private String size;
    @Column(name = "tag")
    private String tag;
    @Column(name = "description")
    private String description;
    @Column(name = "ln")
    private DB.ScriptLN ln;
    @Column(name = "md5")
    private String md5;
    @Column(name = "path")
    private String path;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

    @Transient
    private User createUser;
    @Transient
    private User updateUser;

}
