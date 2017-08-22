package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.ReportDetail;
import com.testwa.distest.server.mvc.model.ReportSdetail;
import com.testwa.distest.server.mvc.repository.ReportSdetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class ReportSdetailService extends BaseService {

    @Autowired
    private ReportSdetailRepository reportSdetailRepository;

    public void save(ReportSdetail testcaseDetail){
        reportSdetailRepository.save(testcaseDetail);
    }

    public void deleteById(String testcaseSdetailId){
        reportSdetailRepository.delete(testcaseSdetailId);
    }

    public ReportSdetail getTestcaseSdetailById(String testcaseSdetailId){
        return reportSdetailRepository.findOne(testcaseSdetailId);
    }

    public Page<ReportSdetail> findAll(PageRequest pageRequest) {
        return reportSdetailRepository.findAll(pageRequest);
    }

    public ReportSdetail findTestcaseSdetailByDetailIdScriptId(String reportDetailId, String scriptId) {
        return reportSdetailRepository.findByDetailIdAndScriptId(reportDetailId, scriptId);
    }

    public List<ReportSdetail> findByDetailId(String detailId) {
        return reportSdetailRepository.findByDetailIdOrderById(detailId);
    }

    public void saveAll(String detailId, List<String> scripts) {
        String username = "";
        for(String scriptId : scripts){
            ReportSdetail sdetail = new ReportSdetail(detailId, scriptId, username);
            this.save(sdetail);
        }
    }

    public Long findSuccessScriptCount(List<ReportDetail> details) {
        List<String> detailIds = new ArrayList<>();
        for(ReportDetail d : details){
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

    public Long findErrorScriptCount(List<ReportDetail> details) {
        List<String> detailIds = new ArrayList<>();
        for(ReportDetail d : details){
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

    public Page<ReportSdetail> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }
}
