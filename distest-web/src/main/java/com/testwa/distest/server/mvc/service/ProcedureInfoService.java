package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.mvc.repository.ProcedureInfoRepository;
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

    public List<ProcedureInfo> findByReportDetailId(String infoId) {
        return procedureInfoRepository.findByReportDetailIdOrderByTimestampAsc(infoId);
    }

    public List<ProcedureInfo> findByReportDetailIdAndScriptId(String infoId, String scriptId) {
        return procedureInfoRepository.findByReportDetailIdAndScriptIdOrderByTimestampAsc(infoId, scriptId);
    }

    public ProcedureInfo findLastProcedureInfo(ProcedureInfo stepInfo) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("reportDetailId").is(stepInfo.getReportDetailId()),
                Criteria.where("scriptId").is(stepInfo.getScriptId()),
                Criteria.where("timestamp").lt(stepInfo.getTimestamp()));
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        ProcedureInfo last = procedureInfoRepository.findOne(new Query(criatira).with(sort));
        return last;
    }

    public Page<ProcedureInfo> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return procedureInfoRepository.find(query, pageRequest);
    }
}
