package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.CrashLog;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.repository.CrashLogRepository;
import com.testwa.distest.server.mongo.repository.ExecutorLogInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by wen on 16/9/7.
 */
@Service
public class CrashLogService extends BaseService {

    @Autowired
    private CrashLogRepository crashLogRepository;

    public void save(CrashLog log){
        crashLogRepository.save(log);
    }

    public List<CrashLog> findByTaskId(Long taskId){
        return crashLogRepository.findByTaskId(taskId);
    }
    public List<CrashLog> findByTaskIdAndDeviceId(Long taskId, String deviceId){
        return crashLogRepository.findByTaskIdAndDeviceId(taskId, deviceId);
    }

}