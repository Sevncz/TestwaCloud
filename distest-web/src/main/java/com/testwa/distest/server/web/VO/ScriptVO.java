package com.testwa.distest.server.web.VO;

import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.TestwaScript;
import com.testwa.distest.server.util.TimeUtil;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.Date;

/**
 * Created by wen on 2016/11/19.
 */
public class ScriptVO {

    private String id;
    private String name;
    private String aliasName;
    private String size;
    private String createDate;
    private String userName;
    private String type;
    private String md5;
    private String appName;
    private String appVersion;
    private String modifyDate;
    private String modifyUserName;
    private String porjectName;

    public ScriptVO(TestwaScript script, TestwaApp app, TestwaProject project) {
        this.id = script.getId();
        this.name = script.getName();
        this.aliasName = script.getAliasName();
        this.size = script.getSize();
        if(script.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(script.getCreateDate().getTime());
        }
        this.userName = script.getUsername();
        this.type = script.getType();
        this.md5 = script.getMd5();
        if(app != null){
            this.appName = app.getName();
            this.appVersion = app.getVersion();
        }
        if(script.getModifyDate() != null){
            this.modifyDate = TimeUtil.formatTimeStamp(script.getModifyDate().getTime());
            this.modifyUserName = script.getModifyUserName();
        }

        this.porjectName = project.getName();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getModifyUserName() {
        return modifyUserName;
    }

    public void setModifyUserName(String modifyUserName) {
        this.modifyUserName = modifyUserName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPorjectName() {
        return porjectName;
    }

    public void setPorjectName(String porjectName) {
        this.porjectName = porjectName;
    }
}
