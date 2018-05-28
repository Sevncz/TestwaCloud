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
public class TaskLoggerService extends BaseService {

    @Autowired
    private TaskLogRepository taskLoggerRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(TaskLog entity){
        taskLoggerRepository.save(entity);
    }

    public void deleteById(String entityId){
        taskLoggerRepository.delete(entityId);
    }

    public void deleteByTaskId(Long taskId){
        taskLoggerRepository.deleteByTaskId(taskId);
    }

    public TaskLog findOneByTaskId(Long taskId){
        return taskLoggerRepository.findByTaskId(taskId);
    }

}
