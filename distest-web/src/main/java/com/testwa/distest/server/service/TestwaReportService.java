package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaDevice;
import com.testwa.distest.server.model.TestwaReport;
import com.testwa.distest.server.repository.TestwaReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TestwaReportService extends BaseService{

    @Autowired
    private TestwaReportRepository reportRepository;

    public void save(TestwaReport report){
        reportRepository.save(report);
    }

    public void deleteById(String reportId){
        reportRepository.delete(reportId);
    }

    public TestwaReport getReportById(String reportId){
        return reportRepository.findOne(reportId);
    }

    public Page<TestwaReport> findAll(PageRequest pageRequest) {
        return reportRepository.findAll(pageRequest);
    }

    public Page<TestwaReport> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return reportRepository.find(query, pageRequest);
    }

}
