package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.CrashLog;
import com.testwa.distest.server.mongo.repository.CrashLogRepository;
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

    public List<CrashLog> findBy(Long taskCode){
        return crashLogRepository.findByTaskCode(taskCode);
    }
    public List<CrashLog> findBy(Long taskCode, String deviceId){
        return crashLogRepository.findByTaskCodeAndDeviceId(taskCode, deviceId);
    }

}
