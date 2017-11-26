package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
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

    private List<DeviceAndroid> devices;
    private DB.TaskStatus status;
    private Long taskSceneId;
    private Long projectId;
    private App app;
    private String testcaseJson; // json
    private String scriptJson; // json

    private Long createBy;
    private Date createTime;
    private Date endTime;
    private String errorMsg;


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
