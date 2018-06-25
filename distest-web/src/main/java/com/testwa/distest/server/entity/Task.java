package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("task")
public class Task extends ProjectBaseEntity {

    @JsonIgnore
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
    private DB.TaskType taskType;
    private Date endTime;
    private Integer outTime;
    private Long taskCode;

    @Column(value="deviceStatusStatis", ignore=true)
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
