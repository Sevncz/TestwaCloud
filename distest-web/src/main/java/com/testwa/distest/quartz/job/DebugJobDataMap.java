package com.testwa.distest.quartz.job;

import lombok.Data;

@Data
public class DebugJobDataMap {
    private String deviceId;
    private Long devLogId;
    private String socketClientId;
}
