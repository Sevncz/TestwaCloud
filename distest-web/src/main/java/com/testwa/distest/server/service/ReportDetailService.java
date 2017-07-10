package com.testwa.distest.server.service;

import com.testwa.distest.server.model.ReportDetail;
import com.testwa.distest.server.repository.ReportDetailRepository;
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
public class ReportDetailService extends BaseService {

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    public void save(ReportDetail testcaseDetail){
        reportDetailRepository.save(testcaseDetail);
    }

    public void deleteById(String testcaseDetailId){
        reportDetailRepository.delete(testcaseDetailId);
    }

    public ReportDetail getTestcaseDetailById(String testcaseDetailId){
        return reportDetailRepository.findOne(testcaseDetailId);
    }

    public Page<ReportDetail> findAll(PageRequest pageRequest) {
        return reportDetailRepository.findAll(pageRequest);
    }

    public List<ReportDetail> findByReportId(String reportId) {
        return reportDetailRepository.findByReportIdOrderByIdDesc(reportId);
    }

    public Page<ReportDetail> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return reportDetailRepository.find(query, pageRequest);
    }
}
