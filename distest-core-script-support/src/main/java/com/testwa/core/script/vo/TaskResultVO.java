package com.testwa.core.script.vo;

import lombok.Data;

import java.util.Map;

@Data
public class TaskResultVO {
    private Long taskCode;
    private String url;
    private String result;
    private String deviceId;
}
