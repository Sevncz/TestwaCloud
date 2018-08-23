package com.testwa.distest.server.web.task.execute;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.cmd.ScriptInfo;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.event.TaskOverEvent;
import com.testwa.distest.server.mongo.model.TaskParams;
import com.testwa.distest.server.mongo.service.TaskParamsService;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.service.task.service.TaskDeviceService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
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
    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private DeviceLockMgr deviceLockMgr;

    // 暂定同时支持100个任务并发
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(100);
    private final ConcurrentHashMap<Long, Future> futures = new ConcurrentHashMap<>();

    /**
     *@Description: 开始执行一个回归测试任务
     *@Param: [deviceIds, projectId, testcaseId, appInfoId, caseName, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public TaskStartResultVO startHG(List<String> useableDevices, App app, List<Long> scriptIds, Long taskCode) {
        Testcase testcase = testcaseService.saveTestcaseByScriptIds(app, scriptIds);
        TaskStartResultVO vo = startHG(useableDevices, app, testcase.getId(), testcase.getCaseName(), taskCode);
        if(scriptIds.size() == 1) {
            testcaseService.delete(testcase.getId());
        }
        return vo;
    }

    public TaskStartResultVO startHG(List<String> deviceIds, App app, Long testcaseId, String caseName, Long taskCode) throws ObjectNotExistsException {
        return start(deviceIds, app.getProjectId(), testcaseId, app.getId(), caseName, DB.TaskType.HG, taskCode);
    }

    /**
     *@Description: 开始执行一个兼容测试任务
     *@Param: [deviceIds, projectId, appInfoId, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public TaskStartResultVO startJR(List<String> deviceIds, Long projectId, Long appId, Long taskCode) throws ObjectNotExistsException {
        return start(deviceIds, projectId, null, appId, "兼容测试", DB.TaskType.JR, taskCode);
    }

    /**
     *@Description: 开始执行一个遍历测试任务
     *@Param: [useableList, projectId, appId, taskCode]
     *@Return: com.testwa.distest.server.web.task.vo.TaskStartResultVO
     *@Author: wen
     *@Date: 2018/7/26
     */
    public TaskStartResultVO startCrawler(List<String> deviceIds, Long projectId, Long appId, Long taskCode) {
        return start(deviceIds, projectId, null, appId, "遍历测试", DB.TaskType.CRAWLER, taskCode);
    }

    private TaskStartResultVO start(List<String> deviceIds, Long projectId, Long testcaseId, Long appId, String taskName, DB.TaskType taskType, Long taskCode) throws ObjectNotExistsException {
        TaskStartResultVO result = new TaskStartResultVO();
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
        }
        task.setScriptJson(JSON.toJSONString(allscript));
        task.setTestcaseJson(JSON.toJSONString(alltestcase));
        task.setTaskName(taskName);

        Map<String, DeviceLog> deviceLogMap = new HashMap<>();
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

            RemoteRunCommand cmd = new RemoteRunCommand();
            AppInfo appInfo = new AppInfo();
            BeanUtils.copyProperties(app, appInfo);
            cmd.setAppInfo(appInfo);
            cmd.setCmd(DB.CommandEnum.START.getValue());
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);
            cmd.setTaskCode(taskCode);
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(key);

            Device d = deviceService.findByDeviceId(key);
            if(observer != null ){
                if(taskType.equals(DB.TaskType.HG)) {
                    final DeviceLog devLog = new DeviceLog(key, DB.DeviceLogType.HG);
                    devLog.setUserCode(user.getUserCode());
                    deviceLogMap.put(key, devLog);
                    Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }else if(taskType.equals(DB.TaskType.JR)) {
                    final DeviceLog devLog = new DeviceLog(key, DB.DeviceLogType.JR);
                    devLog.setUserCode(user.getUserCode());
                    deviceLogMap.put(key, devLog);
                    Message message = Message.newBuilder().setTopicName(Message.Topic.JR_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }else if(taskType.equals(DB.TaskType.CRAWLER)) {
                    final DeviceLog devLog = new DeviceLog(key, DB.DeviceLogType.CRAWLER);
                    devLog.setUserCode(user.getUserCode());
                    deviceLogMap.put(key, devLog);
                    Message message = Message.newBuilder().setTopicName(Message.Topic.CRAWLER_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }
            }else{
                result.addUnableDevice(d);
                log.error("设备 {} - {} 还未准备好", d.getDeviceId(), d.getModel());
                throw new ObjectNotExistsException("设备" + key + "还未准备好");
            }

            deviceService.work(key);
        }
        task.setCreateBy(user.getId());
        task.setCreateTime(new Date());
        task.setEnabled(true);
        task.setDevicesJson(JSON.toJSONString(result.getRunningDevices()));
        task.setStatus(DB.TaskStatus.RUNNING);
        taskService.save(task);


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
                        context.publishEvent(new TaskOverEvent(this, taskCode, true));
                        Future future = futures.get(taskCode);
                        if (future != null) future.cancel(true);
                        return;
                    }
                }

                if(runningCount <= 0) {
                    context.publishEvent(new TaskOverEvent(this, taskCode, false));
                    Future future = futures.get(taskCode);
                    if (future != null) future.cancel(true);
                }
            }
        }, initialDelay, period, TimeUnit.SECONDS);
        futures.put(taskCode, future);
        return result;
    }


    /**
     * 停止任务
     * @param form
     */
    public void stop(TaskStopForm form) {
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
