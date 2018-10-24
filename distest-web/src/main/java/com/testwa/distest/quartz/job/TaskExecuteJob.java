package com.testwa.distest.quartz.job;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.event.TaskOverEvent;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Future;


@Slf4j
@Component
@DisallowConcurrentExecution
public class TaskExecuteJob implements BaseJob, InterruptableJob {
    private boolean _interrupted = false;

    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String paramsStr = dataMap.getString("params");
        ExecuteJobDataMap params = JSON.parseObject(paramsStr, ExecuteJobDataMap.class);

        Long taskCode = params.getTaskCode();

        Task t = taskService.findByCode(taskCode);
        int runningCount = t.getDevices().size();
        while(!_interrupted) {
            List<SubTask> tds = subTaskService.findByTaskCode(taskCode);
            for(SubTask taskDevice : tds){
                if(!DB.TaskStatus.RUNNING.equals(taskDevice.getStatus())
                        && !DB.TaskStatus.NOT_EXECUTE.equals(taskDevice.getStatus())) {
                    runningCount--;
                    deviceLockMgr.workRelease(taskDevice.getDeviceId());
                }
                String deviceId = taskDevice.getDeviceId();
                if(!deviceAuthMgr.isOnline(deviceId)){
                    runningCount--;
                    deviceLockMgr.workRelease(taskDevice.getDeviceId());
                }
            }
            if(runningCount > 0){
                // 检查超时，超过30分钟自动关闭
                DateTime startTime = new DateTime(t.getCreateTime());
                DateTime now = new DateTime();
                Duration d = new Duration(startTime, now);
                if(d.getStandardMinutes() > 30){
                    break;
                }
            }

            if(runningCount <= 0) {
                break;
            }
        }
        applicationContext.publishEvent(new TaskOverEvent(this, taskCode, false));

    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        _interrupted = true;
    }
}