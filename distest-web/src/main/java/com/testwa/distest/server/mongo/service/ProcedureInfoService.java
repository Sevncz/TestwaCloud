package com.testwa.distest.server.mongo.service;

import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureStatisRepository;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class ProcedureInfoService extends BaseService {

    @Autowired
    private ProcedureInfoRepository procedureInfoRepository;
    @Autowired
    private ProcedureStatisRepository procedureStatisRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(ProcedureInfo info){
        procedureInfoRepository.save(info);
    }

    public void deleteById(String infoId){
        procedureInfoRepository.delete(infoId);
    }

    public ProcedureInfo findOne(String infoId){
        return procedureInfoRepository.findOne(infoId);
    }

    public Page<ProcedureInfo> findAll(PageRequest pageRequest) {
        return procedureInfoRepository.findAll(pageRequest);
    }

    public ProcedureInfo findLastProcedureInfo(ProcedureInfo stepInfo) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(stepInfo.getExecutionTaskId()),
                Criteria.where("scriptId").is(stepInfo.getTestSuit()),
                Criteria.where("timestamp").lt(stepInfo.getTimestamp()));
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        ProcedureInfo last = procedureInfoRepository.findOne(new Query(criatira).with(sort));
        return last;
    }

    public Page<ProcedureInfo> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

    public ProcedureStatis getProcedureStatisByExeId(Long exeId){
        return procedureStatisRepository.findByExeId(exeId);
    }

    public void saveProcedureStatis(ProcedureStatis s) {
        procedureStatisRepository.save(s);
    }

    public void deleteStatisById(String taskId) {
        procedureStatisRepository.delete(taskId);
    }

    public List<ProcedureInfo> findBySessionId(String sessionId) {
        return procedureInfoRepository.findBySessionId(sessionId);
    }

    public List<ProcedureInfo> findByExeId(Long taskId) {
        return procedureInfoRepository.findByExecutionTaskIdOrderByTimestampAsc(taskId);
    }

    public PageResult<ProcedureInfo> findByPage(StepPageForm form) {
        Query query = new Query();
        if(form.getScriptId() != null){
            query.addCriteria(Criteria.where("testSuit").is(form.getScriptId()));
        }
        if(form.getTaskId() != null){
            query.addCriteria(Criteria.where("executionTaskId").is(form.getTaskId()));
        }
        if(StringUtils.isNotBlank(form.getDeviceId())){
            query.addCriteria(Criteria.where("deviceId").is(form.getDeviceId()));
        }
        int pageNum = form.getPageNo();
        int rows = form.getPageSize();
        String sortField = "timestamp";
        Sort sort = new Sort(Sort.Direction.ASC, sortField);
        PageRequest pageRequest = new PageRequest(pageNum, rows, sort);
        Page<ProcedureInfo> page = procedureInfoRepository.find(query, pageRequest);
        PageResult<ProcedureInfo> result = new PageResult<>(page.getContent(), page.getTotalElements());
        return result;
    }

    public List<ProcedureInfo> findList(StepListForm form) {
        Query query = new Query();
        if(form.getScriptId() != null){
            query.addCriteria(Criteria.where("testSuit").is(form.getScriptId()));
        }
        if(form.getTaskId() != null){
            query.addCriteria(Criteria.where("executionTaskId").is(form.getTaskId()));
        }
        if(StringUtils.isNotBlank(form.getDeviceId())){
            query.addCriteria(Criteria.where("deviceId").is(form.getDeviceId()));
        }
        return procedureInfoRepository.find(query);
    }

    public ProcedureInfo findNextById(String procedureId) {
        ProcedureInfo pi = procedureInfoRepository.findOne(procedureId);
        Long timeStamp = pi.getTimestamp();

        Query query = new Query();
        String field = "timestamp";
        query.addCriteria(Criteria.where(field).gt(timeStamp));
        query.addCriteria(Criteria.where("sessionId").is(pi.getSessionId()));
        query.limit(1);
        Sort sort = new Sort(Sort.Direction.ASC, field);
        query.with(sort);
        List<ProcedureInfo> nextList = procedureInfoRepository.find(query);
        if(nextList != null && nextList.size() >0){
            return nextList.get(0);
        }
        return null;
    }
}
