package com.testwa.distest.server.web.task.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 16/09/2017.
 */

@Data
public class TaskProgressVO {
    List<TaskProgressLine> lineList = new ArrayList<>();
    List<String> deviceNameList = new ArrayList<>();
    List<String> pointList = new ArrayList<>();

}
