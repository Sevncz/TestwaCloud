package com.testwa.distest.server.web.task.vo;

import lombok.Data;

/**
 * Created by wen on 16/09/2017.
 */

@Data
public class TaskProgressVO {
    private String deviceId;
    private Long scriptId;
    private Long testcaseId;
    private String progress;
}
