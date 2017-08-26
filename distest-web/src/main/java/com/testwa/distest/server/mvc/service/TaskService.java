package com.testwa.distest.server.mvc.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.repository.ExecutionTaskRepository;
import com.testwa.distest.server.mvc.repository.TaskRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TaskService extends BaseService{

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private AppService appService;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ExecutionTaskRepository executionTaskRepository;

    private final SocketIOServer server;

    @Autowired
    public TaskService(SocketIOServer server) {
        this.server = server;
    }

    public void save(Task task){
        taskRepository.save(task);
    }

    public Task getTaskById(String taskId) {
        return taskRepository.findOne(taskId);
    }


    public void deleteById(String taskId){
        taskRepository.delete(taskId);
    }

    public Page<Task> findAll(PageRequest pageRequest) {
        return taskRepository.findAll(pageRequest);
    }

    public Page<Task> findPage(PageRequest pageRequest, String appId, List<String> projectIds) {
        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(appId)){
            andCriteria.add(Criteria.where("appId").is(appId));
        }
        andCriteria.add(Criteria.where("projectId").in(projectIds));
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);
        return taskRepository.find(query, pageRequest);
    }

    public void run(String taskId, List<String> deviceIds) throws Exception {

        ExecutionTask et = new ExecutionTask();
        //  查询任务...
        Task task = getTaskById(taskId);
        RemoteRunCommand params = new RemoteRunCommand();
        params.setAppId(task.getAppId());
        params.setCmd(1);


        et.setTaskId(task.getId());
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
        List<TDevice> tds = new ArrayList<>();
        for(String deviceId : deviceIds){
            TDevice t = deviceService.getDeviceById(deviceId);
            tds.add(t);
        }
        et.setDevices(tds);
        executionTaskRepository.save(et);

        params.setTaskId(et.getId());
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
                    throw new Exception("agentClient not found");
                }
            }else{
                throw new Exception("session not found");
            }
        }


    }

}
