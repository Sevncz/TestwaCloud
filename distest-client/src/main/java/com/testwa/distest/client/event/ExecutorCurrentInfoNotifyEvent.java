package com.testwa.distest.client.event;import lombok.Data;import org.springframework.context.ApplicationEvent;@Datapublic class ExecutorCurrentInfoNotifyEvent extends ApplicationEvent {    private String deviceId;    private Long taskId;    private Long currScriptId;    private Long currTestCaseId;    public ExecutorCurrentInfoNotifyEvent(Object source, String deviceId, Long taskId, Long currScriptId, Long currTestCaseId){        super(source);        this.deviceId = deviceId;        this.taskId = taskId;        this.currScriptId = currScriptId;        this.currTestCaseId = currTestCaseId;    }}