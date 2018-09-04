package com.testwa.distest.server.entity;import com.testwa.core.base.annotation.TableName;import com.testwa.distest.common.enums.DB;import lombok.Data;import java.util.Date;/** * @Program: distest * @Description: 任务详情，对应的是每个设备 * @Author: wen * @Create: 2018-05-02 17:41 **/@Data@TableName("task_device")public class TaskDevice extends ProjectBaseEntity {    private Long taskCode;    private String deviceId;    private DB.TaskType taskType;    private DB.TaskStatus status;    private String errorMsg;    private Date endTime;    private String video;}