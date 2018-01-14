package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("task")
public class Task extends BaseEntity {

    private DB.TaskStatus status;
    private Long taskSceneId;
    private Long projectId;
    private Long appId;
    @JsonIgnore
    private String appJson; // json
    @JsonIgnore
    private String testcaseJson; // json
    @JsonIgnore
    private String scriptJson; // json
    @JsonIgnore
    private String devicesJson; // json

    private String taskName;

    private Long createBy;
    private Date createTime;
    private Date endTime;
    private String errorMsg;


    public App getApp(){
        if(StringUtils.isEmpty(this.appJson)){
            return null;
        }
        return JSON.parseObject(this.appJson, App.class);
    }
    public List<DeviceAndroid> getDevices(){
        if(StringUtils.isEmpty(this.devicesJson)){
            return new ArrayList<>();
        }
        return JSON.parseArray(this.devicesJson, DeviceAndroid.class);
    }

    public List<Testcase> getTestcaseList(){
        if(StringUtils.isEmpty(this.testcaseJson)){
            return new ArrayList<>();
        }
        return JSON.parseArray(this.testcaseJson, Testcase.class);
    }

    public List<Script> getScriptList(){
        if(StringUtils.isEmpty(this.scriptJson)){
            return new ArrayList<>();
        }
        return JSON.parseArray(this.scriptJson, Script.class);
    }

}
