package com.testwa.core.script.vo;

import lombok.Data;

@Data
public class ExcutorVO {
    private String name = "Testwa";
    private String type = "Testwa";
    private String url = "http://cloud.testwa.com";
    private String buildName;
    private String reportName;
}
