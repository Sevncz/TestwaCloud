package com.testwa.distest.server.mvc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
@AllArgsConstructor
public class ProjectStats {
    private Integer device;
    private Integer app;
    private Integer script;
    private Integer testcase;
    private Integer task;
    private Integer report;
}
