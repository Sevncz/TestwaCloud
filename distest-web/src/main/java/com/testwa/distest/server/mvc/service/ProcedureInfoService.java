package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.mvc.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mvc.repository.ProcedureStatisRepository;
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

    public ProcedureInfo getProcedureInfoById(String infoId){
        return procedureInfoRepository.findOne(infoId);
    }

    public Page<ProcedureInfo> findAll(PageRequest pageRequest) {
        return procedureInfoRepository.findAll(pageRequest);
    }

    public ProcedureInfo findLastProcedureInfo(ProcedureInfo stepInfo) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(stepInfo.getExecutionTaskId()),
                Criteria.where("scriptId").is(stepInfo.getScriptId()),
                Criteria.where("timestamp").lt(stepInfo.getTimestamp()));
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        ProcedureInfo last = procedureInfoRepository.findOne(new Query(criatira).with(sort));
        return last;
    }

    public Page<ProcedureInfo> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

    public ProcedureStatis getProcedureStatisByExeId(String exeId){
        return procedureStatisRepository.findByExeId(exeId);
    }

    public void saveProcedureStatis(ProcedureStatis s) {
        procedureStatisRepository.save(s);
    }

    public void deleteStatisById(String exeId) {
        procedureStatisRepository.delete(exeId);
    }

    public List<ProcedureInfo> findBySessionId(String sessionId) {
        return procedureInfoRepository.findBySessionId(sessionId);
    }
}
