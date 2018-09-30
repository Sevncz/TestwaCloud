package com.testwa.distest.server.web.project.vo;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class ProjectStatisMultiBarVO {
    // 名称列表
    private List<String> legend;
    private List<TestCountSeries> series;

    public void add(TestCountSeries testCountSeries) {
        if(series == null) {
            series = new ArrayList<>();
        }
        series.add(testCountSeries);
    }

    public void init() {
        legend = new ArrayList<>();
        series = new ArrayList<>();
    }

    @Data
    public static class TestCountSeries {
        // 测试类型名称
        private String name;
        // 每一个app的进行该类型测试的次数
        private Long[] data;

        public TestCountSeries(Integer dataLength) {
            data = new Long[dataLength];
            for(int i=0;i<dataLength;i++) {
                data[i] = 0L;
            }
        }

        public void add(Long count, int index) {
            data[index] = count;
        }
    }

}
