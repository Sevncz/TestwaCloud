package com.testwa.distest.server.web.task.deploy;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.common.enums.DB;
import com.testwa.core.entity.transfer.RemoteRunCommand;
import com.testwa.core.entity.transfer.RemoteTestcaseContent;
import com.testwa.core.entity.*;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.form.TaskNewDeployForm;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.cache.DeviceCacheMgr;
import com.testwa.distest.server.web.task.vo.TaskProgressVO;
import com.testwa.distest.server.websocket.service.PushCmdService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by wen on 25/10/2017.
 */
@Component
public class DeployMgr {
    private static final Logger log = LoggerFactory.getLogger(DeployMgr.class);

    @Autowired
    private TaskSceneService taskService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService executionTaskService;
    @Autowired
    private DeviceCacheMgr deviceCacheMgr;
    @Autowired
    private PushCmdService pushCmdService;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void newdeploy(TaskNewDeployForm form) {
        log.info(form.toString());
        Long taskId = taskService.save(form);
        TaskStartForm startForm = new TaskStartForm();
        startForm.setDeviceIds(form.getDeviceIds());
        startForm.setTaskId(taskId);
        start(startForm, form.getCaseIds(), form.getAppId());
    }


    public void start(TaskStartForm form, List<Long> caseIds, Long appId) {
        log.info(form.toString());

        List<RemoteTestcaseContent> cases = new ArrayList<>();
        for(Long caseId : caseIds){

            RemoteTestcaseContent content = new RemoteTestcaseContent();
            content.setTestcaseId(caseId);
            Testcase c = testcaseService.findOne(caseId);
            content.setScriptIds(c.getScripts());
            cases.add(content);

        }

        for (String key : form.getDeviceIds()) {

            DeviceBase d = deviceCacheMgr.getDeviceContent(key);
            RemoteRunCommand cmd = new RemoteRunCommand();
            cmd.setAppId(appId);
            cmd.setCmd(DB.CommandEnum.START);
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);
            pushCmdService.startTestcase(cmd, d.getDeviceId());
        }
    }

    public void start(TaskStartForm form) {
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long start(Long projectId, User user, Long taskId, List<String> deviceIds) throws ObjectNotExistsException {

        Task task = new Task();
        task.setProjectId(projectId);
        task.setCreateBy(user.getId());
        //  查询任务...
        TaskScene scene = taskService.findOne(taskId);
        RemoteRunCommand params = new RemoteRunCommand();
        params.setAppId(scene.getAppId());
        params.setCmd(DB.CommandEnum.START);


        task.setTaskSceneId(scene.getId());
        App app = appService.findOne(scene.getAppId());
        task.setApp(app);


        List<RemoteTestcaseContent> testcaseList = new ArrayList<>();
        List<Testcase> testcases = new ArrayList<>();
        Map<Long, List<Script>> scripts = new HashMap<>();
        for(Testcase testcase : scene.getTestcases()){
            RemoteTestcaseContent content = new RemoteTestcaseContent();
            content.setTestcaseId(testcase.getId());
            content.setScriptIds(testcase.getScripts());
            testcaseList.add(content);

            testcases.add(testcase);

            scripts.put(testcase.getId(), testcase.getScripts());
        }
        task.setScripts(scripts);
        task.setTestcases(testcases);
        params.setTestcaseList(testcaseList);
        List<DeviceAndroid> tds = new ArrayList<>();
        for(String deviceId : deviceIds){
            DeviceAndroid t = deviceService.getDeviceById(deviceId);
            tds.add(t);
        }
        task.setDevices(tds);
        task.setStatus(DB.TaskStatus.RUNNING);

        params.setExeId(task.getId());
        // 执行...
        for(String deviceId : deviceIds){

            params.setInstall("");
            params.setDeviceId(deviceId);
            String agentSession = remoteClientService.getMainSessionByDeviceId(deviceId);
            if(StringUtils.isNotBlank(agentSession)){
                SocketIOClient agentClient = server.getClient(UUID.fromString(agentSession));
                if(agentClient != null){
                    agentClient.sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(params));
                }else{
                    task.setStatus(DB.TaskStatus.ERROR);
                    throw new ObjectNotExistsException("agentClient not found");
                }
            }else{
                task.setStatus(DB.TaskStatus.ERROR);
                throw new ObjectNotExistsException("session not found");
            }
        }
        return executionTaskService.save(task);
    }

    public void stop(TaskStopForm form) {
        log.info(form.toString());
    }

    public List<TaskProgressVO> getProgress(Long exeId) {

        Task et = executionTaskService.findOne(exeId);
        List<DeviceAndroid> tds = et.getDevices();
        Map<String, List<Script>> scripts = et.getScripts();
        int testcaseNum = scripts.keySet().size();
        int scriptNum = 0;
        for(List l : scripts.values()){
            scriptNum = scriptNum + l.size();
        }
        List<TaskProgressVO> result = new ArrayList<>();
        int finalScriptNum = scriptNum;
        tds.forEach(d -> {
            TaskProgressVO vo = new TaskProgressVO();
            Long size = remoteClientService.getExeInfoSize(d.getId());
            vo.setProgress(getProgressNum(size, (float)finalScriptNum));
            String content = remoteClientService.getExeInfoProgress(d.getId());
            if(StringUtils.isNotBlank(content)){
                Map<String, String> jsonContent = JSON.parseObject(content, Map.class);
                vo.setDeviceId(d.getDeviceId());
                vo.setScriptId(Long.parseLong(jsonContent.get("srciptId")));
                vo.setTestcaseId(Long.parseLong(jsonContent.get("testcaseId")));
                result.add(vo);
            }
        });
        return result;
    }


    private String getProgressNum(Long exedScriptNum, float allScriptNum){
        float num= exedScriptNum/allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

}
