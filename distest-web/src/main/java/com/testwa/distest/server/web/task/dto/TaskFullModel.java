package com.testwa.distest.server.web.task.dto;

import com.testwa.core.entity.App;
import com.testwa.core.entity.Testcase;
import lombok.Data;

import java.util.List;

/**
 * Created by wen on 25/10/2017.
 */
@Data
public class TaskFullModel {
    private String taskName;
    private App app;
    private List<Testcase> testcases;
}
