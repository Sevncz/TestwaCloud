package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.repository.ExecutorLogInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by wen on 16/9/7.
 */
@Service
public class ExecutorLogInfoService extends BaseService {

    @Autowired
    private ExecutorLogInfoRepository executorLogInfoRepository;

    public void save(ExecutorLogInfo info){
        executorLogInfoRepository.save(info);
    }

    public List<ExecutorLogInfo> findByTaskId(Long taskId){
        return executorLogInfoRepository.findByTaskIdOrderByTimestampAsc(taskId);
    }

}
