package com.testwa.distest.server.mongo.service;

import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.AppiumRunningLogRepository;
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
public class AppiumRunningLogService extends BaseService {

    @Autowired
    private AppiumRunningLogRepository procedureInfoRepository;
    @Autowired
    private ProcedureStatisRepository procedureStatisRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(AppiumRunningLog info){
        procedureInfoRepository.save(info);
    }

    public void deleteById(String infoId){
        procedureInfoRepository.delete(infoId);
    }

    public AppiumRunningLog findOne(String infoId){
        return procedureInfoRepository.findOne(infoId);
    }

    public Page<AppiumRunningLog> findAll(PageRequest pageRequest) {
        return procedureInfoRepository.findAll(pageRequest);
    }

    public AppiumRunningLog findLastProcedureInfo(AppiumRunningLog stepInfo) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(stepInfo.getTaskCode()),
                Criteria.where("scriptId").is(stepInfo.getTestSuit()),
                Criteria.where("timestamp").lt(stepInfo.getTimestamp()));
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        AppiumRunningLog last = procedureInfoRepository.findOne(new Query(criatira).with(sort));
        return last;
    }

    public Page<AppiumRunningLog> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

    public ProcedureStatis getProcedureStatisByExeId(Long taskCode){
        return procedureStatisRepository.findByTaskCode(taskCode);
    }

    public void saveProcedureStatis(ProcedureStatis s) {
        procedureStatisRepository.save(s);
    }

    public void deleteStatisById(String taskCode) {
        procedureStatisRepository.delete(taskCode);
    }

    public List<AppiumRunningLog> findBySessionId(String sessionId) {
        return procedureInfoRepository.findBySessionId(sessionId);
    }

    public List<AppiumRunningLog> findBy(Long taskCode) {
        return procedureInfoRepository.findByTaskCodeOrderByTimestampAsc(taskCode);
    }

    public PageResult<AppiumRunningLog> findByPage(StepPageForm form) {
        if(form.getScriptId() == null){
            throw new ParamsIsNullException("ScriptId is null");
        }
        if(form.getTaskCode() == null){
            throw new ParamsIsNullException("TaskId is null");
        }
        if(StringUtils.isBlank(form.getDeviceId())){
            throw new ParamsIsNullException("DeviceId is null");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("testSuit").is(form.getScriptId()));
        query.addCriteria(Criteria.where("taskCode").is(form.getTaskCode()));
        query.addCriteria(Criteria.where("deviceId").is(form.getDeviceId()));
        int pageNum = form.getPageNo();
        int rows = form.getPageSize();
        String sortField = "timestamp";
        Sort sort = new Sort(Sort.Direction.ASC, sortField);
        PageRequest pageRequest = new PageRequest(pageNum, rows, sort);
        Page<AppiumRunningLog> page = procedureInfoRepository.find(query, pageRequest);
        PageResult<AppiumRunningLog> result = new PageResult<>(page.getContent(), page.getTotalElements());
        return result;
    }

    public List<AppiumRunningLog> findList(StepListForm form) {
        if(form.getScriptId() == null){
            throw new ParamsIsNullException("ScriptId is null");
        }
        if(form.getTaskCode() == null){
            throw new ParamsIsNullException("TaskId is null");
        }
        if(StringUtils.isBlank(form.getDeviceId())){
            throw new ParamsIsNullException("DeviceId is null");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("testSuit").is(form.getScriptId()));
        query.addCriteria(Criteria.where("taskCode").is(form.getTaskCode()));
        query.addCriteria(Criteria.where("deviceId").is(form.getDeviceId()));
        return procedureInfoRepository.find(query);
    }

    public AppiumRunningLog findNextById(String procedureId) {
        AppiumRunningLog pi = procedureInfoRepository.findOne(procedureId);
        Long timeStamp = pi.getTimestamp();

        Query query = new Query();
        String field = "timestamp";
        query.addCriteria(Criteria.where(field).gt(timeStamp));
        query.addCriteria(Criteria.where("sessionId").is(pi.getSessionId()));
        query.limit(1);
        Sort sort = new Sort(Sort.Direction.ASC, field);
        query.with(sort);
        List<AppiumRunningLog> nextList = procedureInfoRepository.find(query);
        if(nextList != null && nextList.size() >0){
            return nextList.get(0);
        }
        return null;
    }

    public List<AppiumRunningLog> findByTaskCodeAndDeviceId(Long taskCode, String deviceId) {
        return procedureInfoRepository.findByTaskCodeAndDeviceIdOrderByTimestampAsc(taskCode, deviceId);
    }
}
