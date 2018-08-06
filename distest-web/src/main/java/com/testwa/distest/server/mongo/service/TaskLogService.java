package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.TaskLog;
import com.testwa.distest.server.mongo.repository.TaskLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TaskLogService extends BaseService {

    @Autowired
    private TaskLogRepository taskLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(TaskLog entity){
        taskLogRepository.save(entity);
    }

    public void deleteBy(String entityId){
        taskLogRepository.delete(entityId);
    }

    public void deleteBy(Long taskCode){
        taskLogRepository.deleteByTaskCode(taskCode);
    }

    public TaskLog findOneByTaskCode(Long taskCode){
        return taskLogRepository.findByTaskCode(taskCode);
    }

}
