package com.testwa.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@Data
public class RemoteRunCommand {

    private String exeId;
    private String appId;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private String install;
    private int cmd;  // 0 关闭，1 启动

}
