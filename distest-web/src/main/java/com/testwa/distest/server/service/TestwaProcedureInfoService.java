package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaProcedureInfo;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.repository.TestwaProcedureInfoRepository;
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
public class TestwaProcedureInfoService extends BaseService {

    @Autowired
    private TestwaProcedureInfoRepository procedureInfoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(TestwaProcedureInfo info){
        procedureInfoRepository.save(info);
    }

    public void deleteById(String infoId){
        procedureInfoRepository.delete(infoId);
    }

    public TestwaProcedureInfo getProcedureInfoById(String infoId){
        return procedureInfoRepository.findOne(infoId);
    }

    public Page<TestwaProcedureInfo> findAll(PageRequest pageRequest) {
        return procedureInfoRepository.findAll(pageRequest);
    }

    public List<TestwaProcedureInfo> findByReportDetailId(String infoId) {
        return procedureInfoRepository.findByReportDetailIdOrderByTimestampAsc(infoId);
    }

    public List<TestwaProcedureInfo> findByReportDetailIdAndScriptId(String infoId, String scriptId) {
        return procedureInfoRepository.findByReportDetailIdAndScriptIdOrderByTimestampAsc(infoId, scriptId);
    }

    public TestwaProcedureInfo findLastProcedureInfo(TestwaProcedureInfo stepInfo) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("reportDetailId").is(stepInfo.getReportDetailId()),
                Criteria.where("scriptId").is(stepInfo.getScriptId()),
                Criteria.where("timestamp").lt(stepInfo.getTimestamp()));
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        TestwaProcedureInfo last = procedureInfoRepository.findOne(new Query(criatira).with(sort));
        return last;
    }

    public Page<TestwaProcedureInfo> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return procedureInfoRepository.find(query, pageRequest);
    }
}
