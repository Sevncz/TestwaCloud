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

    private String appName;
    @JsonIgnore
    private String aliasName;
    private String packageName;
    private String activity;
    private String sdkVersion;
    private String targetSdkVersion;
    private String version; // app version
    private String applicationLable;
    private String applicationIcon;
    private DB.PhoneOS osType;
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

}
