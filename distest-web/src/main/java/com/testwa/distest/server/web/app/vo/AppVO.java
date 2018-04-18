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
    private String fileName;
    private String path;
    private Long projectId;
    private String md5;
    private String size;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

    private String displayName; // 应用显示名称
    private String icon;  // 应用图标
    private String packageName;  // 应用包名
    private String version;  // 应用版本
    private String miniOSVersion;  // 最低系统版本要求
    private String platformVersion;  // 系统版本
    private String sdkBuild;  // sdk版本
    private String activity;  // 启动包

}
