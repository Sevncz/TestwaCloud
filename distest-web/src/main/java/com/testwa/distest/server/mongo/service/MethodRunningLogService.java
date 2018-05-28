package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.MethodRunningLog;
import com.testwa.distest.server.mongo.repository.MethodRunningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


/**
 * Created by wen on 16/9/7.
 */
@Service
public class MethodRunningLogService extends BaseService {

    @Autowired
    private MethodRunningLogRepository executorLogInfoRepository;

    public void save(MethodRunningLog info){
        executorLogInfoRepository.save(info);
    }

    public List<MethodRunningLog> findByTaskId(Long taskId){
        return executorLogInfoRepository.findByTaskIdOrderByTimestampAsc(taskId);
    }

    public List<MethodRunningLog> findShowStep(Long taskId) {

        List<Integer> orders = Arrays.asList(0,1,2,3,6);

        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskId").is(taskId),
                Criteria.where("methodOrder").in(orders));
        Sort sort = new Sort(Sort.Direction.ASC, "methodOrder","timestamp");
        Query query = new Query();
        query.addCriteria(criatira);
        query.with(sort);
        return executorLogInfoRepository.find(query);
    }

    public List<MethodRunningLog> findByTaskIdAndDeviceId(Long taskId, String deviceId) {
        return executorLogInfoRepository.findByTaskIdAndDeviceIdOrderByMethodOrderAsc(taskId, deviceId);
    }
}
