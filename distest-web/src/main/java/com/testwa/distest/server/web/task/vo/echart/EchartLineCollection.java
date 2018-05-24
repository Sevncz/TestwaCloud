package com.testwa.distest.server.web.task.vo.echart;import lombok.Data;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: 符合echart线性图表显示的格式 * { *                     name: ['nubia', 'xiaomo'], *                     value: [ *                         [ *                             ["2018-03-27 22:17:04", 18], *                             ["2018-03-27 22:17:04", 18], *                             ["2018-03-27 22:17:04", 18] *                         ], *                         [ *                             ["2018-03-27 22:17:04", 18], *                             ["2018-03-27 22:17:04", 18], *                             ["2018-03-27 22:17:04", 18] *                         ], *                     ] *                 } * @Author: wen * @Create: 2018-05-24 11:11 **/@Datapublic class EchartLineCollection {    private List<String> name = new ArrayList<>();    private List<List<Object[]>> value = new ArrayList<>();    public void addLine(EchartLine line) {        this.name.add(line.getName());        this.value.add(line.getPoints());    }}