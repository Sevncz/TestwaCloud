package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.Performance;
import com.testwa.distest.server.mongo.repository.PerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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
