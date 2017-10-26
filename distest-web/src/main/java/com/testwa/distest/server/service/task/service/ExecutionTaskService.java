package com.testwa.distest.server.service.task.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.exception.NoSuchExecutionTaskException;
import com.testwa.distest.server.mvc.entity.*;
import com.testwa.distest.server.service.task.dao.IExecutionTaskDAO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by wen on 24/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ExecutionTaskService {

    @Autowired
    private IExecutionTaskDAO executionTaskDAO;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long run(Long projectId, User user, Long taskId, List<String> deviceIds) throws Exception {

        ExecutionTask et = new ExecutionTask();
        et.setProjectId(projectId);
        et.setCreator(user.getId());
        //  查询任务...
        Task task = getTaskById(taskId);
        RemoteRunCommand params = new RemoteRunCommand();
        params.setAppId(task.getAppId());
        params.setCmd(1);


        et.setTaskId(task.getId());
        et.setTaskName(task.getName());
        App app = appService.getAppById(task.getAppId());
        et.setApp(app);


        List<RemoteTestcaseContent> testcaseList = new ArrayList<>();
        List<Testcase> testcases = new ArrayList<>();
        Map<String, List<Script>> scripts = new HashMap<>();
        for(String testcaseId : task.getTestcaseIds()){
            Testcase testcase = testcaseService.getTestcaseById(testcaseId);
            if(testcase != null){
                RemoteTestcaseContent content = new RemoteTestcaseContent();
                content.setTestcaseId(testcaseId);
                content.setScriptIds(testcase.getScripts());
                testcaseList.add(content);

                testcases.add(testcase);

                List<Script> temp = new ArrayList<>();
                for(String scriptId : testcase.getScripts()){
                    Script s = scriptService.getScriptById(scriptId);
                    temp.add(s);
                }
                scripts.put(testcaseId, temp);
            }
        }
        et.setScripts(scripts);
        et.setTestcases(testcases);
        params.setTestcaseList(testcaseList);
        List<DeviceAndroid> tds = new ArrayList<>();
        for(String deviceId : deviceIds){
            DeviceAndroid t = deviceService.getDeviceById(deviceId);
            tds.add(t);
        }
        et.setDevices(tds);
        et.setStatus(ExecutionTask.StatusEnum.START.getCode());

        params.setExeId(et.getId());
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
                    et.setStatus(ExecutionTask.StatusEnum.ERROR.getCode());
                    throw new Exception("agentClient not found");
                }
            }else{
                et.setStatus(ExecutionTask.StatusEnum.ERROR.getCode());
                throw new Exception("session not found");
            }
        }
        return executionTaskDAO.insert(et);
    }


    private String getProgressNum(Long exedScriptNum, float allScriptNum){
        float num= exedScriptNum/allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

    public List<ExecutionTask> getRunningTask(Long projectId, User user) {
        return executionTaskDAO.findByProjectIdAndCreatorAndStatusIn(projectId, user.getId(), ExecutionTask.StatusEnum.notFinishedCode,
                new PageRequest(0, 20, Sort.Direction.DESC, "createTime")).getContent();
    }

    public List<ExecutionTask> getRecentFinishedRunningTask(Long projectId, User user) {
        return executionTaskDAO.findByProjectIdAndCreatorAndStatusIn(projectId, user.getId(), ExecutionTask.StatusEnum.finishedCode,
                new PageRequest(0, 20, Sort.Direction.DESC, "endTime")).getContent();
    }

    public String createTaskQuickAndDeploy(String projectId, User user, String appId, String caseId, List<String> devices) throws Exception{
        Task task = new Task();
        task.setType(TypeEnum.Quick.getCode());
        task.setAppId(appId);
        task.setName("quick deploy");
        task.setProjectId(projectId);
        task.setTestcaseIds(new ArrayList<>(Arrays.asList(caseId)));
        task.setDescription("quick deploy");
        task.setCreateDate(TimeUtil.getTimestampLong());
        task.setModifyDate(TimeUtil.getTimestampLong());
        task.setCreator(user.getId());
        task.setDisable(false);
        task = save(task);

        return run(projectId, user, task.getId(), devices).getId();
    }

    public ProcedureStatis executionTaskStatis(String exeId) throws NoSuchExecutionTaskException {

        ExecutionTask et = this.getExeTaskById(exeId);
        if(et == null){
            throw new NoSuchExecutionTaskException("没有该执行任务");
        }
        return procedureStatisRepository.findByExeId(exeId);
    }


    public void save(ExecutionTask exeTask) {
        executionTaskDAO.insert(exeTask);
    }


    public ExecutionTask findOne(Long exeId) {
        return executionTaskDAO.findOne(exeId);
    }

    public List<ExecutionTask> findAll(List<Long> entityIds) {
        return executionTaskDAO.findAll(entityIds);
    }
}
