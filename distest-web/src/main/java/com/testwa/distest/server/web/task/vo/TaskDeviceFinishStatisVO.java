package com.testwa.distest.server.web.task.vo;import lombok.Data;import java.util.List;import java.util.Map;/** * @Program: distest * @Description: 设备完成统计 * @Author: wen * @Create: 2018-05-17 13:38 **/@Datapublic class TaskDeviceFinishStatisVO {    private List<TaskDeviceFinishGraph> graph;    private TaskDeviceFinishStatistics statistics;}