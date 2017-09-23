package com.testwa.distest.server.mvc.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.exception.NoSuchAppException;
import com.testwa.distest.server.exception.NoSuchExecutionTaskException;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.repository.ExecutionTaskRepository;
import com.testwa.distest.server.exception.NoSuchTaskException;
import com.testwa.distest.server.exception.NoSuchTestcaseException;
import com.testwa.distest.server.mvc.api.TaskController;
import com.testwa.distest.server.mvc.model.Task;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.repository.ProcedureStatisRepository;
import com.testwa.distest.server.mvc.repository.TaskRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.mvc.vo.ExeTaskProgressVO;
import com.testwa.distest.server.mvc.vo.TaskVO;
import com.testwa.distest.server.mvc.vo.TestcaseVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TaskService extends BaseService{

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ExecutionTaskRepository executionTaskRepository;
    @Autowired
    private TestcaseRepository testcaseRepository;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private AppService appService;
    @Autowired
    private ProcedureStatisRepository procedureStatisRepository;

    private final SocketIOServer server;

    @Autowired
    public TaskService(SocketIOServer server) {
        this.server = server;
    }

    public Task save(Task task){
        return taskRepository.save(task);
    }

    public Task getTaskById(String taskId) {
        return taskRepository.findOne(taskId);
    }

    public ExecutionTask getExeTaskById(String exeId) {
        return executionTaskRepository.findOne(exeId);
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

    public TaskVO getTaskVO(String taskId) {
        Task task = taskRepository.findOne(taskId);
        TaskVO taskVO = new TaskVO();
        BeanUtils.copyProperties(task, taskVO);

        //get app
        taskVO.setApp(this.appService.getAppVOById(task.getAppId()));

        List<Testcase> testcases = new ArrayList<>();
        task.getTestcaseIds().forEach(caseId ->{
            testcases.add( this.testcaseRepository.findOne(caseId));
        });
        List<TestcaseVO> testcaseVOs = new ArrayList<>();
        testcases.forEach(testcase -> {
            TestcaseVO testcaseVO = new TestcaseVO();
            BeanUtils.copyProperties(testcase, testcaseVO );
            testcaseVOs.add(testcaseVO);
        });
        taskVO.setTestcaseVOs(testcaseVOs);
        taskVO.setCreateDate(TimeUtil.formatTimeStamp(task.getCreateDate()));
        taskVO.setModifyDate(TimeUtil.formatTimeStamp(task.getModifyDate()));

        return taskVO;
    }

    public void modifyTask(TaskController.TaskInfo modifyTaskVO) throws NoSuchTaskException, NoSuchTestcaseException,NoSuchAppException{
        Task task = taskRepository.findOne(modifyTaskVO.getTaskId());
        if (task == null) {
            throw new NoSuchTaskException("无此任务！");
        }

        List<String> caseIds = modifyTaskVO.getCaseIds();
        this.testcaseService.checkTestcases(modifyTaskVO.getCaseIds());
        this.appService.checkApp(modifyTaskVO.getAppId());
        task.setAppId(modifyTaskVO.getAppId());
        task.setTestcaseIds(caseIds);
        task.setDescription(modifyTaskVO.getDescription());
        task.setModifyDate(TimeUtil.getTimestampLong());
        task.setName(modifyTaskVO.getName());
        taskRepository.save(task);
    }
    public ExecutionTask run(String projectId, User user, String taskId, List<String> deviceIds) throws Exception {

        ExecutionTask et = new ExecutionTask();
        et.setProjectId(projectId);
        et.setCreator(user.getId());
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
        return executionTaskRepository.save(et);
    }

    public void saveExetask(ExecutionTask exeTask) {
        executionTaskRepository.save(exeTask);
    }

    public List<Task> getTaskByProjectId(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public Integer getCountTaskByProjectId(String projectId) {
        return taskRepository.countByProjectId(projectId);
    }

    public List<ExeTaskProgressVO> getProgress(String exeId) throws NoSuchExecutionTaskException {

        ExecutionTask et = this.getExeTaskById(exeId);
        if(et == null){
            throw new NoSuchExecutionTaskException("没有该执行任务");
        }
        List<TDevice> tds = et.getDevices();
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

    private String getProgressNum(Long exedScriptNum, float allScriptNum){
        float num= exedScriptNum/allScriptNum;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        return df.format(num);
    }

    public List<ExecutionTask> getRunningTask(String projectId, User user) {
        return executionTaskRepository.findByProjectIdAndCreatorAndStatusIn(projectId, user.getId(), ExecutionTask.StatusEnum.notFinishedCode,
                new PageRequest(0, 20, Sort.Direction.DESC, "createTime")).getContent();
    }

    public List<ExecutionTask> getRecentFinishedRunningTask(String projectId, User user) {
        return executionTaskRepository.findByProjectIdAndCreatorAndStatusIn(projectId, user.getId(), ExecutionTask.StatusEnum.finishedCode,
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

    public void executionTaskStatis(String exeId) throws NoSuchExecutionTaskException {

        ExecutionTask et = this.getExeTaskById(exeId);
        if(et == null){
            throw new NoSuchExecutionTaskException("没有该执行任务");
        }
        ProcedureStatis ps = procedureStatisRepository.findByExeId(exeId);
        ps.getCpurateInfo();

    }
}
