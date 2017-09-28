package com.testwa.distest.server.mvc.service;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.server.exception.NoSuchExecutionTaskException;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.repository.ExecutionTaskRepository;
import com.testwa.distest.server.mvc.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mvc.repository.ProcedureStatisRepository;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.mvc.vo.ExeTaskProgressVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class ExeTaskService extends BaseService{

    @Autowired
    private ExecutionTaskRepository executionTaskRepository;
    @Autowired
    private ProcedureStatisRepository procedureStatisRepository;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private ProcedureInfoRepository procedureInfoRepository;

    public Page<ExecutionTask> findPage(PageRequest pageRequest, User user, String projectId) {
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where("projectId").is(projectId));

        Query query = buildQueryByCriteria(andCriteria, null);
        return executionTaskRepository.find(query, pageRequest);
    }

    public ExecutionTask getExeTaskById(String exeId) {
        return executionTaskRepository.findOne(exeId);
    }

    public ProcedureStatis executionTaskStatis(String exeId) throws NoSuchExecutionTaskException {

        ExecutionTask et = this.getExeTaskById(exeId);
        if(et == null){
            throw new NoSuchExecutionTaskException("没有该执行任务");
        }
        return procedureStatisRepository.findByExeId(exeId);
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

    public List<ProcedureInfo> getSteps(String deviceId, String exeId, String caseId, String scriptId) {
        return procedureInfoRepository.findByDeviceIdAndExecutionTaskIdAndTestcaseIdAndScriptId(deviceId, exeId, caseId, scriptId);
    }
}
