package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 16/8/30.
 */
@Data
@TableName("app")
public class App extends ProjectBaseEntity {

    private String fileName;
    @JsonIgnore
    private String fileAliasName;
    @JsonIgnore
    private String path;
    // 上传创建者
    private String md5;
    private String size;
    private String description;
    @Column(value="createUser", ignore=true)
    private User createUser;
    @Column(value="updateUser", ignore=true)
    private User updateUser;

    private String displayName; // 应用显示名称
    private String icon;  // 应用图标
    private String packageName;  // 应用包名
    private String version;  // 应用版本
    private String miniOSVersion;  // 支持的最低系统版本
    private String platformVersion;  // 系统版本
    private String sdkBuild;  // sdk版本
    private String activity;  // 启动包

    private DB.PhoneOS platform;

}
