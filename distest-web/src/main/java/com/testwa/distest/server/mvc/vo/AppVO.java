package com.testwa.distest.server.mvc.vo;


import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.App;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wen on 2016/11/19.
 */
@Data
@NoArgsConstructor
public class AppVO {

    private String id;
    private String name;
    private String aliasName;
    private String packageName;
    private String activity;
    private String sdkVersion;
    private String targetSdkVersion;
    private String version;
    private String type;
    private String projectId;
    private String project;
    private String md5;
    private String size;
    private String createDate;

    private String userId;
    private String userName;

    public AppVO(App app){
        this.id = app.getId();
        this.name = app.getName();
        this.aliasName = app.getAliasName();
        this.packageName = app.getPackageName();
        this.activity = app.getActivity();
        this.sdkVersion = app.getSdkVersion();
        this.targetSdkVersion = app.getTargetSdkVersion();
        this.version = app.getVersion();
        this.type = app.getType();
        this.projectId = app.getProjectId();
        this.project = app.getProjectName();
        this.userId = app.getUserId();
        this.md5 = app.getMd5();
        this.size = app.getSize();
        if(app.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(app.getCreateDate().getTime());
        }
        this.userName = app.getUsername();
    }
}
