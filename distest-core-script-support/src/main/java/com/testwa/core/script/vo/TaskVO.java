package com.testwa.core.script.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskVO {

    /**
     * 脚本详情
     */
    private List<ScriptCaseVO> scriptCases;
    /**
     * 需要下载的app路径
     */
    private String appUrl;
    /**
     * taskCode，用于生成目录结构
     */
    private Long taskCode;
    private String deviceId;
    private Map<String, String> metadata;

}
