package com.testwa.core.entity;


import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
import com.testwa.core.common.enums.DB;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wen on 16/8/30.
 */
@Data
@TableName("app")
public class App extends BaseEntity {

    private String appName;
    private String aliasName;
    private String packageName;
    private String activity;
    private String sdkVersion;
    private String targetSdkVersion;
    private String version; // app version
    private DB.PhoneOS type;
    private String path;
    private Long projectId;
    // 上传创建者
    private String md5;
    private String size;
    private String description;
    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;

    private Boolean enabled;

}
