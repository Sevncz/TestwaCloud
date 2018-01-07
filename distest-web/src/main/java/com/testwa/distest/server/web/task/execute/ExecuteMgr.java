package com.testwa.distest.server.web.task.execute;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.TaskStartByTestcaseForm;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
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
public class ExecuteMgr {
    private static final Logger log = LoggerFactory.getLogger(ExecuteMgr.class);

    @Autowired
    private TaskSceneService taskSceneService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private TaskCacheMgr taskCacheMgr;
    @Autowired
    private ClientSessionMgr clientSessionMgr;
    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ScriptService scriptService;


    /**
     * 保存并执行一个任务
     * @param form
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void start(TaskStartByTestcaseForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        Long taskSceneId = taskSceneService.save(form);
        TaskStartForm startForm = new TaskStartForm();
        startForm.setDeviceIds(form.getDeviceIds());
        startForm.setTaskSceneId(taskSceneId);
        start(startForm, form.getCaseIds(), form.getAppId());
    }

    private void start(TaskStartForm form, List<Long> caseIds, Long appId) throws ObjectNotExistsException {
        log.info(form.toString());
        // 记录task的执行信息
        App app = appService.findOne(appId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Task task = new Task();
        task.setTaskSceneId(form.getTaskSceneId());
        task.setProjectId(app.getProjectId());
        task.setAppId(app.getId());
        task.setAppJson(JSON.toJSONString(app));
        List<Long> allscriptId = new ArrayList<>();
        List<Script> allscript = new ArrayList<>();
        List<Testcase> alltestcase = new ArrayList<>();

        List<RemoteTestcaseContent> cases = new ArrayList<>();
        for(Long caseId : caseIds){
            RemoteTestcaseContent content = new RemoteTestcaseContent();
            content.setTestcaseId(caseId);
            Testcase c = testcaseService.fetchOne(caseId);
            List<Long> scriptIds = new ArrayList<>();
            c.getTestcaseDetails().forEach( s -> {
                scriptIds.add(s.getScriptId());
            });
            content.setScriptIds(scriptIds);
            cases.add(content);

            allscriptId.addAll(scriptIds);
            alltestcase.add(c);

        }
        allscript = scriptService.findAll(allscriptId);
        task.setScriptJson(JSON.toJSONString(allscript));
        task.setTestcaseJson(JSON.toJSONString(alltestcase));

        List<DeviceAndroid> alldevice = deviceService.findAllDeviceAndroid(form.getDeviceIds());
        task.setDevicesJson(JSON.toJSONString(alldevice));
        task.setStatus(DB.TaskStatus.RUNNING);
        task.setCreateBy(user.getId());
        task.setCreateTime(new Date());
        Long taskId = taskService.save(task);

        for (String key : form.getDeviceIds()) {

            Device d = deviceService.findByDeviceId(key);
            RemoteRunCommand cmd = new RemoteRunCommand();
            cmd.setAppId(appId);
            cmd.setCmd(DB.CommandEnum.START.getValue());
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);
            cmd.setExeId(taskId);
            pushCmdService.startTestcase(cmd, d.getLastUserId());
        }
    }

    public void start(TaskStartForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        TaskScene ts = taskSceneService.fetchOne(form.getTaskSceneId());

        TaskStartForm startForm = new TaskStartForm();
        startForm.setDeviceIds(form.getDeviceIds());
        startForm.setTaskSceneId(ts.getId());

        List<Long> caseIds = new ArrayList<>();
        ts.getTaskSceneDetails().forEach( t -> {
            caseIds.add(t.getTestcaseId());
        });

        start(startForm, caseIds, ts.getAppId());
    }

    /**
     * 停止任务
     * @param form
     */
    public void stop(TaskStopForm form) {
        log.info(form.toString());
    }

    /**
     * 获得任务进度
     * @param exeId
     * @return
     */
    public List<TaskProgressVO> getProgress(Long exeId) {

        Task task = taskService.findOne(exeId);
        List<DeviceAndroid> tds = task.getDevices();
        int testcaseNum = task.getTestcaseList().size();
        int scriptNum = task.getScriptList().size();
        List<TaskProgressVO> result = new ArrayList<>();
        tds.forEach(d -> {
            TaskProgressVO vo = new TaskProgressVO();
            Long size = taskCacheMgr.getExeInfoSize(d.getDeviceId());
            vo.setProgress(getProgressNum(size, (float)scriptNum));
            String content = null;
            try {
                content = taskCacheMgr.getExeInfoProgress(d.getDeviceId());
            } catch (Exception e) {
                log.error("get task cache error", e);
            }
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
