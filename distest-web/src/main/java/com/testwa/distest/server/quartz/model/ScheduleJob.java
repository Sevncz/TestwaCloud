package com.testwa.distest.server.quartz.model;

import java.io.Serializable;

public class ScheduleJob implements Serializable {
     /** 任务id **/
    private String jobId;
 
    /** 任务名称 **/
    private String jobName;
 
    /** 任务分组 **/
    private String jobGroup;
 
    /** 任务状态 0禁用 1启用 2删除**/
    private String jobStatus;
 
    /** 任务运行时间表达式 **/
    private String cronExpression;
 
    /** 任务描述 **/
    private String desc;

    /** Spring 注入的类名 **/
    private String targetObject;

    /** 方法 **/
    private String targetMethod;

    /** 参数 **/
    private Object[] targetParamsValue;

    /** 是否集群运行 **/
    private int isCluster;


    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getJobGroup()
    {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }

    public String getJobStatus()
    {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus)
    {
        this.jobStatus = jobStatus;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public int getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(int isCluster) {
        this.isCluster = isCluster;
    }

    public Object[] getTargetParamsValue() {
        if(targetParamsValue == null){
            targetParamsValue = new Object[]{};
        }
        return targetParamsValue;
    }

    public void setTargetParamsValue(Object[] targetParamsValue) {
        this.targetParamsValue = targetParamsValue;
    }
}