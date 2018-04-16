package com.testwa.distest.server.web.app.vo;


import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by wen on 2016/11/19.
 */
@Data
@NoArgsConstructor
public class AppVO {

    private Long id;
    private String appName;
    private String aliasName;
    private String packageName;
    private String activity;
    private String sdkVersion;
    private String targetSdkVersion;
    private String version; // app version
    private DB.PhoneOS osType;
    private String path;
    private Long projectId;
    private String md5;
    private String size;
    private String description;
    private String applicationLable;
    private String applicationIcon;
    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

}
