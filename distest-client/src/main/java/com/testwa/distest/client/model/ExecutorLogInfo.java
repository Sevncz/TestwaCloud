package com.testwa.distest.client.model;import lombok.Data;@Datapublic class ExecutorLogInfo {    private String deviceId;    private Long taskId;    private String methodDesc;    private String methodName;    private String args;    private String flag;    private Long time;    private int order;}