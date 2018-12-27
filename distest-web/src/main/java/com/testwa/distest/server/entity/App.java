package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

/**
 * Created by wen on 16/8/30.
 */
@Data
@Table(name= "app" )
public class App extends ProjectBaseEntity {

    @Column(name = "fileName")
    private String fileName;
    @JsonIgnore
    @Column(name = "fileAliasName")
    private String fileAliasName;
    @Column(name = "path")
    private String path;
    // 上传创建者
    @Column(name = "md5")
    private String md5;
    @Column(name = "size")
    private String size;
    @Column(name = "description")
    private String description;
    @Transient
    @Column(name="createUser")
    private User createUser;
    @Transient
    @Column(name="updateUser")
    private User updateUser;

    @Column(name = "displayName")
    private String displayName; // 应用显示名称
    @Column(name = "icon")
    private String icon;  // 应用图标
    @Column(name = "packageName")
    private String packageName;  // 应用包名

//    private AppHis newversion;
    @Column(name = "version")
    private String version;  // 应用版本
    @Column(name = "miniOSVersion")
    private String miniOSVersion;  // 支持的最低系统版本
    @Column(name = "platformVersion")
    private String platformVersion;  // 系统版本
    @Column(name = "sdkBuild")
    private String sdkBuild;  // sdk版本
    @Column(name = "activity")
    private String activity;  // 启动包

    @Column(name = "platform")
    private DB.PhoneOS platform;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

}
