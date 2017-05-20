package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaReportDetail;
import com.testwa.distest.server.model.TestwaReportSdetail;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.repository.TestwaReportSdetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TestwaReportSdetailService extends BaseService {

    @Autowired
    private TestwaReportSdetailRepository reportSdetailRepository;

    public void save(TestwaReportSdetail testcaseDetail){
        reportSdetailRepository.save(testcaseDetail);
    }

    public void deleteById(String testcaseSdetailId){
        reportSdetailRepository.delete(testcaseSdetailId);
    }

    public TestwaReportSdetail getTestcaseSdetailById(String testcaseSdetailId){
        return reportSdetailRepository.findOne(testcaseSdetailId);
    }

    public Page<TestwaReportSdetail> findAll(PageRequest pageRequest) {
        return reportSdetailRepository.findAll(pageRequest);
    }

    public TestwaReportSdetail findTestcaseSdetailByDetailIdScriptId(String reportDetailId, String scriptId) {
        return reportSdetailRepository.findByDetailIdAndScriptId(reportDetailId, scriptId);
    }

    public List<TestwaReportSdetail> findByDetailId(String detailId) {
        return reportSdetailRepository.findByDetailIdOrderById(detailId);
    }

    public void saveAll(String detailId, List<String> scripts) {
        String username = "";
        for(String scriptId : scripts){
            TestwaReportSdetail sdetail = new TestwaReportSdetail(detailId, scriptId, username);
            this.save(sdetail);
        }
    }

    public Long findSuccessScriptCount(List<TestwaReportDetail> details) {
        List<String> detailIds = new ArrayList<>();
        for(TestwaReportDetail d : details){
            detailIds.add(d.getId());
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("detailId").in(detailIds));
        query.addCriteria(Criteria.where("stepStatus").is(0));
        return reportSdetailRepository.count(query);
    }

    public Long findSuccessScriptCount(String detailId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("detailId").is(detailId));
        query.addCriteria(Criteria.where("stepStatus").is(0));
        return reportSdetailRepository.count(query);
    }

    public Long findErrorScriptCount(List<TestwaReportDetail> details) {
        List<String> detailIds = new ArrayList<>();
        for(TestwaReportDetail d : details){
            detailIds.add(d.getId());
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("detailId").in(detailIds));
        query.addCriteria(Criteria.where("stepStatus").gt(0));
        return reportSdetailRepository.count(query);
    }

    public Long findErrorScriptCount(String detailId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("detailId").is(detailId));
        query.addCriteria(Criteria.where("stepStatus").gt(0));
        return reportSdetailRepository.count(query);
    }

    public Page<TestwaReportSdetail> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return reportSdetailRepository.find(query, pageRequest);
    }
}
