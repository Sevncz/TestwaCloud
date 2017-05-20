package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaReportDetail;
import com.testwa.distest.server.model.TestwaReportSdetail;
import com.testwa.distest.server.repository.TestwaReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TestwaReportDetailService extends BaseService {

    @Autowired
    private TestwaReportDetailRepository reportDetailRepository;

    public void save(TestwaReportDetail testcaseDetail){
        reportDetailRepository.save(testcaseDetail);
    }

    public void deleteById(String testcaseDetailId){
        reportDetailRepository.delete(testcaseDetailId);
    }

    public TestwaReportDetail getTestcaseDetailById(String testcaseDetailId){
        return reportDetailRepository.findOne(testcaseDetailId);
    }

    public Page<TestwaReportDetail> findAll(PageRequest pageRequest) {
        return reportDetailRepository.findAll(pageRequest);
    }

    public List<TestwaReportDetail> findByReportId(String reportId) {
        return reportDetailRepository.findByReportIdOrderByIdDesc(reportId);
    }

    public Page<TestwaReportDetail> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return reportDetailRepository.find(query, pageRequest);
    }
}
