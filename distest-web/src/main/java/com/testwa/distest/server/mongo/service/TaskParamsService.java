package com.testwa.distest.server.mongo.service;

import com.testwa.distest.server.mongo.model.TaskLog;
import com.testwa.distest.server.mongo.model.TaskParams;
import com.testwa.distest.server.mongo.repository.TaskParamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TaskParamsService extends BaseService {

    @Autowired
    private TaskParamsRepository taskParamsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(TaskParams entity){
        taskParamsRepository.save(entity);
    }

    public void deleteBy(String entityId){
        taskParamsRepository.delete(entityId);
    }

    public void deleteBy(Long taskCode){
        taskParamsRepository.deleteByTaskCode(taskCode);
    }

    public TaskParams findOneByTaskCode(Long taskCode){
        return taskParamsRepository.findByTaskCode(taskCode);
    }

}
