package com.testwa.distest.server.web.task.execute;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.cmd.ScriptInfo;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.event.TaskOverEvent;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.service.task.service.TaskDeviceService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by wen on 25/10/2017.
 */
@Component
@Slf4j
public class ExecuteMgr {

    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDeviceService taskDeviceService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ApplicationContext context;

    // 暂定同时支持100个任务并发
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(100);
    private final ConcurrentHashMap<Long, Future> futures = new ConcurrentHashMap<>();

    /**
     *@Description: 开始执行一个回归测试任务
     *@Param: [deviceIds, projectId, testcaseId, appId, caseName, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public void startHG(List<String> deviceIds, Long projectId, Long testcaseId, Long appId, String caseName, Long taskCode) throws ObjectNotExistsException {
        start(deviceIds, projectId, testcaseId, appId, caseName, DB.TaskType.HG, taskCode);
    }

    /**
     *@Description: 开始执行一个兼容测试任务
     *@Param: [deviceIds, projectId, appId, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public void startJR(List<String> deviceIds, Long projectId, Long appId, Long taskCode) throws ObjectNotExistsException {
        start(deviceIds, projectId, null, appId, "兼容测试", DB.TaskType.JR, taskCode);
    }

    private Long start(List<String> deviceIds, Long projectId, Long testcaseId, Long appId, String taskName, DB.TaskType taskType, Long taskCode) throws ObjectNotExistsException {
        // 记录task的执行信息
        App app = appService.findOne(appId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Task task = new Task();
        task.setTaskCode(taskCode);
        task.setTaskType(taskType);
        task.setProjectId(projectId);
        task.setAppId(app.getId());
        task.setAppJson(JSON.toJSONString(app));

        List<Script> allscript = new ArrayList<>();
        List<Testcase> alltestcase = new ArrayList<>();
        List<RemoteTestcaseContent> cases = new ArrayList<>();
        RemoteTestcaseContent content = new RemoteTestcaseContent();
        if(testcaseId != null){

            content.setTestcaseId(testcaseId);
            Testcase c = testcaseService.fetchOne(testcaseId);
            // 批量获取案例下的所有脚本
            List<ScriptInfo> scripts = new ArrayList<>();
            List<Long> scriptIds = new ArrayList<>();
            c.getTestcaseDetails().forEach( s -> {
                scriptIds.add(s.getScriptId());
            });
            // 转换成cmd下的scriptInfo
            List<Script> caseAllScript = scriptService.findAll(scriptIds);
            caseAllScript.forEach(script -> {
                ScriptInfo info = new ScriptInfo();
                BeanUtils.copyProperties(script, info);
                scripts.add(info);
            });
            content.setScripts(scripts);
            cases.add(content);

            allscript.addAll(caseAllScript);
            alltestcase.add(c);
            task.setScriptJson(JSON.toJSONString(allscript));
            task.setTestcaseJson(JSON.toJSONString(alltestcase));
        }
        task.setTaskName(taskName);
        List<Device> alldevice = deviceService.findAll(deviceIds);
        task.setDevicesJson(JSON.toJSONString(alldevice));
        task.setCreateBy(user.getId());
        task.setCreateTime(new Date());
        task.setEnabled(true);
        taskService.save(task);

        // 启动任务
        for (String key : deviceIds) {
            TaskDevice taskDevice = new TaskDevice();
            taskDevice.setStatus(DB.TaskStatus.RUNNING);
            taskDevice.setDeviceId(key);
            taskDevice.setTaskCode(taskCode);
            taskDevice.setTaskType(task.getTaskType());
            taskDevice.setCreateTime(new Date());
            taskDevice.setCreateBy(user.getId());
            taskDevice.setEnabled(true);
            taskDevice.setProjectId(app.getProjectId());
            taskDeviceService.save(taskDevice);

//            Device d = deviceService.findByDeviceId(key);
            RemoteRunCommand cmd = new RemoteRunCommand();
            AppInfo appInfo = new AppInfo();
            BeanUtils.copyProperties(app, appInfo);
            cmd.setAppInfo(appInfo);
            cmd.setCmd(DB.CommandEnum.START.getValue());
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);
            cmd.setTaskCode(taskCode);
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(key);
            if(observer != null ){
                if(taskType.equals(DB.TaskType.HG)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                }else if(taskType.equals(DB.TaskType.JR)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.JR_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                }
            }else{
                log.error("设备{}还未准备好", key);
                throw new ObjectNotExistsException("设备" + key + "还未准备好");
            }

            deviceService.work(key);
        }


        int initialDelay = 0;
        int period = 10;
        Future future = scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                log.info("........ get task {} info", taskCode);
                Task t = taskService.findByCode(taskCode);
                int runningCount = t.getDevices().size();
                List<TaskDevice> tds = taskDeviceService.findByTaskCode(taskCode);
                for(TaskDevice taskDevice : tds){
                    if(!DB.TaskStatus.RUNNING.equals(taskDevice.getStatus())
                            && !DB.TaskStatus.NOT_EXECUTE.equals(taskDevice.getStatus())) {
                        runningCount--;
                    }
                    String deviceId = taskDevice.getDeviceId();
                    if(!deviceAuthMgr.isOnline(deviceId)){
                        runningCount--;
                    }
                }
                if(runningCount > 0){
                    // 检查超时，超过30分钟自动关闭
                    DateTime startTime = new DateTime(t.getCreateTime());
                    DateTime now = new DateTime();
                    Duration d = new Duration(startTime, now);
                    if(d.getStandardMinutes() > 30){
                        runningCount = 0;
                    }
                }

                if(runningCount <= 0) {
                    context.publishEvent(new TaskOverEvent(this, taskCode));
                    Future future = futures.get(taskCode);
                    if (future != null) future.cancel(true);
                }
            }
        }, initialDelay, period, TimeUnit.SECONDS);
        futures.put(taskCode, future);

        return taskCode;
    }


    /**
     * 停止任务
     * @param form
     */
    public void stop(TaskStopForm form) {
        log.info(form.toString());
        User currentUser = userService.findByUsername(WebUtil.getCurrentUsername());
        Task task = taskService.findByCode(form.getTaskCode());
        taskService.update(task);
        // 如果传了设备ID，那么停止这几个设备上的任务
        // 否则，停止所有设备上的任务
        if(form.getDeviceIds() != null && form.getDeviceIds().size() > 0){
            for (String key : form.getDeviceIds()) {
                Device d = deviceService.findByDeviceId(key);
                stopDeviceTask(d, form.getTaskCode(), currentUser.getId());
            }
        }else{
            List<Device> deviceList = task.getDevices();
            for (Device d : deviceList) {
                stopDeviceTask(d, form.getTaskCode(), currentUser.getId());
            }
        }
    }

    private void stopDeviceTask(Device device, Long taskCode, Long updateBy) {

        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(device.getDeviceId());
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_CANCEL).setStatus("OK").setMessage(ByteString.copyFromUtf8(String.valueOf(taskCode))).build();
            observer.onNext(message);
        }else{
            log.error("设备还未准备好");
        }

        taskDeviceService.cancelOneTask(device.getDeviceId(), taskCode, updateBy);
        deviceService.release(device.getDeviceId());
    }


    private String getProgressNum(Long exedScriptNum, float allScriptNum){
        float num= exedScriptNum/allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

}
