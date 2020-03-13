package com.testwa.core.script.vo;

import lombok.Data;

@Data
public class TaskEnvVO {
    private String deviceId;
    private String osVersion;
    private String javaVersion;
    private String pythonVersion;
    private String nodeVersion;
    private String agentVersion;
}
