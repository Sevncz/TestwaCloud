package com.testwa.distest.server.mvc.vo;

import lombok.Data;

/**
 * Created by wen on 16/09/2017.
 */

@Data
public class ExeTaskProgressVO {
    private String deviceId;
    private String scriptId;
    private String testcaseId;
    private String progress;
}
