package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Table(name= "task" )
public class Task extends ProjectBaseEntity {

    @JsonIgnore
    @Column(name = "appId")
    private Long appId;
    @JsonIgnore
    @Column(name = "appJson")
    private String appJson; // json
    @JsonIgnore
    @Column(name = "testcaseJson")
    private String testcaseJson; // json
    @JsonIgnore
    @Column(name = "scriptJson")
    private String scriptJson; // json
    @JsonIgnore
    @Column(name = "devicesJson")
    private String devicesJson; // json
    @Column(name = "taskName")
    private String taskName;
    @Column(name = "taskType")
    private DB.TaskType taskType;
    @Column(name = "endTime")
    private Date endTime;
    @Column(name = "outTime")
    private Integer outTime;
    @Column(name = "taskCode")
    private Long taskCode;
    @Column(name = "status")
    private DB.TaskStatus status;



    @Transient
    private Map<String, Integer> deviceStatusStatis;

    public App getApp(){
        if(StringUtils.isEmpty(this.appJson)){
            return null;
        }
        return JSON.parseObject(this.appJson, App.class);
    }
    public List<Device> getDevices(){
        if(StringUtils.isEmpty(this.devicesJson)){
            return new ArrayList<>();
        }
        return JSON.parseArray(this.devicesJson, Device.class);
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

    public void setDeviceStatusStatis(List<TaskDeviceStatusStatis> tds) {
        this.deviceStatusStatis = new HashMap<>();
        tds.forEach(t -> {
            this.deviceStatusStatis.put(String.valueOf(t.getTaskStatus().getValue()), t.getCount());
        });
    }
}
