package com.testwa.distest.server.mvc.vo;

import lombok.Data;

/**
 * Created by wen on 16/09/2017.
 */

@Data
public class ExeTaskProgressVO {
    private String deviceId;
    private Long scriptId;
    private Long testcaseId;
    private String progress;
}
