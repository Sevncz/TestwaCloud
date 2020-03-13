package com.testwa.distest.server.web.task.mgr;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.util.CronDateUtils;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.cmd.ScriptInfo;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.exception.DeviceException;
import com.testwa.distest.quartz.job.ExecuteJobDataMap;
import com.testwa.distest.quartz.service.JobService;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.cache.mgr.TaskCountMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.script.service.ScriptMetadataService;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.agent.Message;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 25/10/2017.
 */
@Component
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ExecuteMgr {
    private final static String JOB_EXECUTE_NAME = "com.testwa.distest.quartz.job.TaskExecuteJob";

    @Autowired
    private AppService appService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private JobService jobService;
    @Autowired
    private TaskCountMgr taskCountMgr;
    @Autowired
    private SnowflakeIdWorker taskIdWorker;
    @Autowired
    private User currentUser;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ScriptMetadataService scriptMetadataService;

    /**
     * @Description: 开始执行一个回归测试任务
     * @Param: [deviceIds, projectId, testcaseId, appInfoId, caseName, taskCode]
     * @Return: void
     * @Author: wen
     * @Date: 2018/6/4
     */
    @Transactional(rollbackFor = BusinessException.class, propagation = Propagation.REQUIRED)
    public TaskStartResultVO startFunctionalTestTask(List<String> useableDevices, App app, List<Long> scriptIds) {
        Testcase testcase = testcaseService.saveTestcaseByScriptIds(app, scriptIds);
        TaskStartResultVO vo = startFunctionalTestTask(useableDevices, app, testcase.getId(), testcase.getCaseName());
        if (scriptIds.size() == 1) {
            testcaseService.delete(testcase.getId());
        }
        return vo;
    }

    @Transactional(rollbackFor = BusinessException.class, propagation = Propagation.REQUIRED)
    public TaskStartResultVO startFunctionalTestTask(List<String> deviceIds, App app, Long testcaseId, String caseName) {
        return startTask(deviceIds, app.getProjectId(), testcaseId, app.getId(), caseName, DB.TaskType.FUNCTIONAL);
    }

    /**
     * @Description: 开始执行一个兼容测试任务
     * @Param: [deviceIds, projectId, appInfoId, taskCode]
     * @Return: void
     * @Author: wen
     * @Date: 2018/6/4
     */
    @Transactional(rollbackFor = BusinessException.class, propagation = Propagation.REQUIRED)
    public TaskStartResultVO startCompabilityTestTask(List<String> deviceIds, Long projectId, Long appId) {
        return startTask(deviceIds, projectId, null, appId, "兼容测试", DB.TaskType.COMPATIBILITY);
    }

    /**
     * @Description: 开始执行一个遍历测试任务
     * @Param: [useableList, projectId, appId, taskCode]
     * @Return: com.testwa.distest.server.web.task.vo.TaskStartResultVO
     * @Author: wen
     * @Date: 2018/7/26
     */
    @Transactional(rollbackFor = BusinessException.class, propagation = Propagation.REQUIRED)
    public TaskStartResultVO startCrawlerTestTask(List<String> deviceIds, Long projectId, Long appId) {
        return startTask(deviceIds, projectId, null, appId, "遍历测试", DB.TaskType.CRAWLER);
    }

    private TaskStartResultVO startTask(List<String> deviceIds, Long projectId, Long testcaseId, Long appId, String taskName, DB.TaskType taskType) {
        // 记录task的执行信息
        App app = appService.get(appId);
        Long taskCode = taskIdWorker.nextId();
        TaskStartResultVO result = new TaskStartResultVO();
        result.setTaskCode(taskCode);
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
        if (testcaseId != null) {

            content.setTestcaseId(testcaseId);
            Testcase c = testcaseService.fetchOne(testcaseId);
            // 批量获取案例下的所有脚本
            List<ScriptInfo> scripts = new ArrayList<>();
            List<Long> scriptIds = new ArrayList<>();
            if (c != null && c.getTestcaseDetails() != null) {
                c.getTestcaseDetails().forEach(s -> {
                    scriptIds.add(s.getScriptId());
                });
            } else {
                log.error("testcase {} is null or testcase detail is null", testcaseId);
            }
            // 转换成cmd下的scriptInfo
            List<Script> caseAllScript = scriptService.findAll(scriptIds);
            caseAllScript.forEach(script -> {
                ScriptInfo info = new ScriptInfo();
                if (script != null) {
                    BeanUtils.copyProperties(script, info);
                    scripts.add(info);
                }
            });
            content.setScripts(scripts);
            cases.add(content);

            allscript.addAll(caseAllScript);
            alltestcase.add(c);
        }
        task.setScriptJson(JSON.toJSONString(allscript));
        task.setTestcaseJson(JSON.toJSONString(alltestcase));
        task.setTaskName(taskName);

        // 启动任务
        for (String key : deviceIds) {

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
            if (observer != null) {
                if (taskType.equals(DB.TaskType.FUNCTIONAL)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                } else if (taskType.equals(DB.TaskType.COMPATIBILITY)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.JR_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                } else if (taskType.equals(DB.TaskType.CRAWLER)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.CRAWLER_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }
            } else {
                result.addUnableDevice(d);
                log.error("设备 {} - {} 还未准备好", d.getDeviceId(), d.getModel());
                throw new DeviceException(ResultCode.ILLEGAL_OP, "设备" + key + "还未准备好");
            }

            SubTask subTask = new SubTask();
            subTask.setStatus(DB.TaskStatus.RUNNING);
            subTask.setDeviceId(key);
            subTask.setTaskCode(taskCode);
            subTask.setTaskType(task.getTaskType());
            subTask.setCreateTime(new Date());
            subTask.setCreateBy(currentUser.getId());
            subTask.setEnabled(true);
            subTask.setProjectId(app.getProjectId());
            subTaskService.insert(subTask);
            deviceService.work(key);
            taskCountMgr.incrSubTaskCount(taskCode);
        }
        task.setCreateBy(currentUser.getId());
        task.setCreateTime(new Date());
        task.setEnabled(true);
        task.setDevicesJson(JSON.toJSONString(result.getRunningDevices()));
        task.setStatus(DB.TaskStatus.RUNNING);
        taskService.insert(task);


        // 执行任务所需要的参数
        ExecuteJobDataMap params = new ExecuteJobDataMap();
        params.setTaskCode(taskCode);

        DateTime now = new DateTime();
        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());
        jobService.addJob(JOB_EXECUTE_NAME, String.valueOf(taskCode), cron, task.getTaskType().getDesc() + "，任务ID：" + taskCode, JSON.toJSONString(params));
        return result;
    }


    /**
     * 停止任务
     *
     * @param form
     */
    @Transactional(rollbackFor = BusinessException.class, propagation = Propagation.REQUIRED)
    public void stop(TaskStopForm form) {
        Task task = taskService.findByCode(form.getTaskCode());
        taskService.update(task);
        // 如果传了设备ID，那么停止这几个设备上的任务
        // 否则，停止所有设备上的任务
        if (form.getDeviceIds() != null && !form.getDeviceIds().isEmpty()) {
            for (String key : form.getDeviceIds()) {
                Device d = deviceService.findByDeviceId(key);
                stopDeviceTask(d, form.getTaskCode(), currentUser.getId());
            }
        } else {
            List<Device> deviceList = task.getDevices();
            for (Device d : deviceList) {
                stopDeviceTask(d, form.getTaskCode(), currentUser.getId());
            }
        }
        jobService.interrupt(JOB_EXECUTE_NAME, String.valueOf(form.getTaskCode()));

    }

    private void stopDeviceTask(Device device, Long taskCode, Long updateBy) {

        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(device.getDeviceId());
        if (observer != null) {
            Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_CANCEL).setStatus("OK").setMessage(ByteString.copyFromUtf8(String.valueOf(taskCode))).build();
            observer.onNext(message);
        } else {
            log.error("设备还未准备好");
        }

        subTaskService.cancelOneTask(device.getDeviceId(), taskCode, updateBy);
        deviceService.release(device.getDeviceId());
    }


    private String getProgressNum(Long exedScriptNum, float allScriptNum) {
        float num = exedScriptNum / allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

    @Transactional(rollbackFor = Exception.class)
    public TaskStartResultVO startFunctionalTestTaskV2(List<String> deviceIds, App app, ScriptCaseVO scriptCaseDetailVO) {
        // 记录task的执行信息
        Long taskCode = taskIdWorker.nextId();
        TaskStartResultVO result = new TaskStartResultVO();
        result.setTaskCode(taskCode);
        Task task = new Task();
        task.setTaskCode(taskCode);
        task.setTaskType(DB.TaskType.FUNCTIONAL);
        task.setProjectId(app.getProjectId());
        task.setAppId(app.getId());
        task.setAppJson(JSON.toJSONString(app));
        task.setScriptJson(JSON.toJSONString(scriptCaseDetailVO));
        task.setTaskName("执行[" + scriptCaseDetailVO.getScriptCaseName() + "]");

        Map<String, String> map = scriptMetadataService.getPython();
        TaskVO taskVO = new TaskVO();
        taskVO.setScriptCase(scriptCaseDetailVO);
        taskVO.setAppUrl(app.getPath());
        taskVO.setTaskCode(taskCode);
        taskVO.setMetadata(map);

        // 启动任务
        for (String key : deviceIds) {
            // 发布任务
            RTopic topic = redissonClient.getTopic(key);
            Device d = deviceService.findByDeviceId(key);
            if(topic == null) {
                result.addUnableDevice(d);
            }else{
                result.addRunningDevice(d);
                taskVO.setDeviceId(key);
                topic.publish(taskVO);
                deviceService.work(key);
                redissonClient.getAtomicLong("task::number::" + taskCode).addAndGet(1);
            }
        }
        task.setCreateBy(currentUser.getId());
        task.setCreateTime(new Date());
        task.setEnabled(true);
        task.setDevicesJson(JSON.toJSONString(result.getRunningDevices()));
        task.setStatus(DB.TaskStatus.RUNNING);
        task.setDevicesJson(JSON.toJSONString(result.getRunningDevices()));
        taskService.insert(task);

        return result;

    }
}
