package com.testwa.distest.server.mvc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
public class DashboardPlatformVO {
    private Stats stats;
    private List<ProjectVO> projects;
    private List<UserDeviceHis> devices;

    public DashboardPlatformVO(Integer projectCounts, Integer deviceCounts, List<ProjectVO> projectVOs, List<UserDeviceHis> devices) {
        this.stats = new Stats(projectCounts,deviceCounts);
        this.projects = projectVOs;
        this.devices = devices;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class Stats {
        private Integer project;
        private Integer device;
    }
}
