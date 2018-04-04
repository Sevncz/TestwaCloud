package com.testwa.distest.server.web.project.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
@AllArgsConstructor
public class ProjectStatis {
    private Long app;
    private Long script;
    private Long testcase;
    private Long task;
    private Long device;
}
