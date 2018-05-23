package com.testwa.distest.server.mongo.service;

import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.mongo.model.Performance;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.PerformanceRepository;
import com.testwa.distest.server.mongo.repository.PerformanceRepository;
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
public class PerformanceService extends BaseService {

    @Autowired
    private PerformanceRepository performanceRepository;

    public void save(Performance info){
        performanceRepository.save(info);
    }

    public void deleteById(String infoId){
        performanceRepository.delete(infoId);
    }

    public Performance findOne(String infoId){
        return performanceRepository.findOne(infoId);
    }

    public Page<Performance> findAll(PageRequest pageRequest) {
        return performanceRepository.findAll(pageRequest);
    }


    public List<Performance> findByTaskId(Long taskId) {
        return performanceRepository.findByTaskIdOrderByTimestampAsc(taskId);
    }

    public List<Performance> findByTaskIdAndDeviceId(Long taskId, String deviceId) {
        return performanceRepository.findByTaskIdAndDeviceIdOrderByTimestampAsc(taskId, deviceId);
    }

}
