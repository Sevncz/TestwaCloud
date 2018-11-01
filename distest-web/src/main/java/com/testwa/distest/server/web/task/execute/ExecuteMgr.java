package com.testwa.distest.server.web.task.execute;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.util.CronDateUtils;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.cmd.ScriptInfo;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.quartz.exception.BusinessException;
import com.testwa.distest.quartz.job.ExecuteJobDataMap;
import com.testwa.distest.quartz.service.JobService;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by wen on 25/10/2017.
 */
@Component
@Slf4j
public class ExecuteMgr {
    private final static String JOB_EXECUTE_NAME = "com.testwa.distest.quartz.job.TaskExecuteJob";
    private final static String JOB_GROUP = "EXECUTE";

    @Autowired
    private UserService userService;
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

    /**
     *@Description: 开始执行一个回归测试任务
     *@Param: [deviceIds, projectId, testcaseId, appInfoId, caseName, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public TaskStartResultVO startFunctionalTestTask(List<String> useableDevices, App app, List<Long> scriptIds, Long taskCode) {
        Testcase testcase = testcaseService.saveTestcaseByScriptIds(app, scriptIds);
        TaskStartResultVO vo = startFunctionalTestTask(useableDevices, app, testcase.getId(), testcase.getCaseName(), taskCode);
        if(scriptIds.size() == 1) {
            testcaseService.delete(testcase.getId());
        }
        return vo;
    }

    public TaskStartResultVO startFunctionalTestTask(List<String> deviceIds, App app, Long testcaseId, String caseName, Long taskCode) throws ObjectNotExistsException {
        return startTask(deviceIds, app.getProjectId(), testcaseId, app.getId(), caseName, DB.TaskType.FUNCTIONAL, taskCode);
    }

    /**
     *@Description: 开始执行一个兼容测试任务
     *@Param: [deviceIds, projectId, appInfoId, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/4
     */
    public TaskStartResultVO startCompabilityTestTask(List<String> deviceIds, Long projectId, Long appId, Long taskCode) throws ObjectNotExistsException {
        return startTask(deviceIds, projectId, null, appId, "兼容测试", DB.TaskType.COMPATIBILITY, taskCode);
    }

    /**
     *@Description: 开始执行一个遍历测试任务
     *@Param: [useableList, projectId, appId, taskCode]
     *@Return: com.testwa.distest.server.web.task.vo.TaskStartResultVO
     *@Author: wen
     *@Date: 2018/7/26
     */
    public TaskStartResultVO startCrawlerTestTask(List<String> deviceIds, Long projectId, Long appId, Long taskCode) {
        return startTask(deviceIds, projectId, null, appId, "遍历测试", DB.TaskType.CRAWLER, taskCode);
    }

    private TaskStartResultVO startTask(List<String> deviceIds, Long projectId, Long testcaseId, Long appId, String taskName, DB.TaskType taskType, Long taskCode) throws ObjectNotExistsException {
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

        // 启动任务
        for (String key : deviceIds) {
            SubTask subTask = new SubTask();
            subTask.setStatus(DB.TaskStatus.RUNNING);
            subTask.setDeviceId(key);
            subTask.setTaskCode(taskCode);
            subTask.setTaskType(task.getTaskType());
            subTask.setCreateTime(new Date());
            subTask.setCreateBy(user.getId());
            subTask.setEnabled(true);
            subTask.setProjectId(app.getProjectId());
            subTaskService.save(subTask);

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
                if(taskType.equals(DB.TaskType.FUNCTIONAL)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }else if(taskType.equals(DB.TaskType.COMPATIBILITY)) {
                    Message message = Message.newBuilder().setTopicName(Message.Topic.JR_TASK_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd))).build();
                    observer.onNext(message);
                    result.addRunningDevice(d);
                }else if(taskType.equals(DB.TaskType.CRAWLER)) {
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


        // 执行任务所需要的参数
        ExecuteJobDataMap params = new ExecuteJobDataMap();
        params.setTaskCode(taskCode);

        DateTime now = new DateTime();
        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());
        try {
            jobService.addJob(JOB_EXECUTE_NAME, String.valueOf(taskCode), cron,  task.getTaskType().getDesc() + "，任务ID：" + taskCode, JSON.toJSONString(params));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
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
        try {
            jobService.interrupt(JOB_EXECUTE_NAME, String.valueOf(form.getTaskCode()));
        } catch (BusinessException e) {
            e.printStackTrace();
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

        subTaskService.cancelOneTask(device.getDeviceId(), taskCode, updateBy);
        deviceService.release(device.getDeviceId());
    }


    private String getProgressNum(Long exedScriptNum, float allScriptNum){
        float num= exedScriptNum/allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

}
