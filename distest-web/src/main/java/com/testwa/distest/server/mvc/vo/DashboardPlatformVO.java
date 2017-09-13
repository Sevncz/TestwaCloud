package com.testwa.distest.server.mvc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
public class DashboardPlatformVO {
    private Stats stats;

    public DashboardPlatformVO() {
        this.stats = new Stats();
    }

    public DashboardPlatformVO(Integer projectCounts, Integer deviceCounts) {
        this.stats = new Stats(projectCounts,deviceCounts);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class Stats {
        private Integer project;
        private Integer device;
    }
}
