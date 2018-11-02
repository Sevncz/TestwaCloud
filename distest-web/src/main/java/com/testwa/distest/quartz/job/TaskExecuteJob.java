package com.testwa.distest.quartz.job;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.event.TaskOverEvent;
import com.testwa.distest.server.service.cache.mgr.TaskCountMgr;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@DisallowConcurrentExecution
public class TaskExecuteJob implements BaseJob, InterruptableJob {
    private boolean _interrupted = false;

    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TaskCountMgr taskCountMgr;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String paramsStr = dataMap.getString("params");
        ExecuteJobDataMap params = JSON.parseObject(paramsStr, ExecuteJobDataMap.class);

        Long taskCode = params.getTaskCode();

        Task t = taskService.findByCode(taskCode);
        boolean timeout = false;
        while(!_interrupted) {
            Integer taskCount = taskCountMgr.getSubTaskCount(taskCode);
            if(taskCount > 0){
                // 检查超时，超过30分钟自动关闭
                DateTime startTime = new DateTime(t.getCreateTime());
                DateTime now = new DateTime();
                Duration d = new Duration(startTime, now);
                if(d.getStandardMinutes() > 30){
                    timeout = true;
                    break;
                }
            }

            if(taskCount <= 0) {
                break;
            }
        }
        applicationContext.publishEvent(new TaskOverEvent(this, taskCode, timeout));

    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        _interrupted = true;
    }
}