package com.testwa.distest.server.web.task.deploy;

import com.alibaba.fastjson.JSON;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.entity.*;
import com.testwa.distest.server.mvc.vo.ExeTaskProgressVO;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.form.TaskNewDeployForm;
import com.testwa.distest.server.service.task.service.ExecutionTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.cache.DeviceCacheMgr;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wen on 25/10/2017.
 */
@Component
public class DeployMgr {
    private static final Logger log = LoggerFactory.getLogger(DeployMgr.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private ExecutionTaskService executionTaskService;
    @Autowired
    private DeviceCacheMgr deviceCacheMgr;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void deploy(TaskNewDeployForm form) {
        log.info(form.toString());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Long taskId = taskService.save(form);
        TaskStartForm startForm = new TaskStartForm();
        startForm.setDeviceIds(form.getDeviceIds());
        startForm.setTaskId(taskId);
        start(startForm);
    }


    public void start(TaskStartForm form) {
        log.info(form.toString());

        List<RemoteTestcaseContent> cases = new ArrayList<>();
        for(Long caseId : form.getCaseIds()){

            RemoteTestcaseContent content = new RemoteTestcaseContent();
            content.setTestcaseId(caseId);
            Testcase c = testcaseService.findOne(caseId);
            content.setScriptIds(c.getScripts());
            cases.add(content);

        }

        for (String key : form.getDeviceIds()) {

            DeviceAndroid d = deviceCacheMgr.getDeviceAndroid(key);

            String sessionId = remoteClientService.getClientSessionByDeviceId(key);

            RemoteRunCommand cmd = new RemoteRunCommand();
            cmd.setAppId(appId);
            cmd.setCmd(RemoteRunCommand.CommandEnum.START);
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);

            server.getClient(UUID.fromString(sessionId))
                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
        }
    }

    public void stop(TaskStopForm form) {
        log.info(form.toString());
    }

    public List<ExeTaskProgressVO> getProgress(Long exeId) {

        ExecutionTask et = executionTaskService.findOne(exeId);
        List<DeviceAndroid> tds = et.getDevices();
        Map<String, List<Script>> scripts = et.getScripts();
        int testcaseNum = scripts.keySet().size();
        int scriptNum = 0;
        for(List l : scripts.values()){
            scriptNum = scriptNum + l.size();
        }
        List<ExeTaskProgressVO> result = new ArrayList<>();
        int finalScriptNum = scriptNum;
        tds.forEach(d -> {
            ExeTaskProgressVO vo = new ExeTaskProgressVO();
            Long size = remoteClientService.getExeInfoSize(d.getId());
            vo.setProgress(getProgressNum(size, (float)finalScriptNum));
            String content = remoteClientService.getExeInfoProgress(d.getId());
            if(StringUtils.isNotBlank(content)){
                Map<String, String> jsonContent = JSON.parseObject(content, Map.class);
                vo.setDeviceId(d.getId());
                vo.setScriptId(jsonContent.get("srciptId"));
                vo.setTestcaseId(jsonContent.get("testcaseId"));
                result.add(vo);
            }
        });
        return result;
    }
}
