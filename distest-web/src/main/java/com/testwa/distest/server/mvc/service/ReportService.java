package com.testwa.distest.server.mvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class ReportService extends BaseService{

    @Autowired
    private ReportRepository reportRepository;

    public void save(Report report){
        reportRepository.save(report);
    }

    public void deleteById(String reportId){
        reportRepository.delete(reportId);
    }

    public Report getReportById(String reportId){
        return reportRepository.findOne(reportId);
    }

    public Page<Report> findAll(PageRequest pageRequest) {
        return reportRepository.findAll(pageRequest);
    }

    public Page<Report> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

}
