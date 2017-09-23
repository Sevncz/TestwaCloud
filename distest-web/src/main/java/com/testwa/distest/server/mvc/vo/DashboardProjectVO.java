package com.testwa.distest.server.mvc.vo;

import com.testwa.distest.server.mvc.model.ExecutionTask;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
@AllArgsConstructor
public class DashboardProjectVO {
    private ProjectStats stats;
    private List<ExecutionTask> runningTask;
    private List<ExecutionTask> recentFinishedTask;
}
