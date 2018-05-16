package com.testwa.distest.server.mongo.service;

import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.model.TaskLogger;
import com.testwa.distest.server.mongo.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureStatisRepository;
import com.testwa.distest.server.mongo.repository.TaskLoggerRepository;
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
public class TaskLoggerService extends BaseService {

    @Autowired
    private TaskLoggerRepository taskLoggerRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(TaskLogger entity){
        taskLoggerRepository.save(entity);
    }

    public void deleteById(String entityId){
        taskLoggerRepository.delete(entityId);
    }

    public void deleteByTaskId(Long taskId){
        taskLoggerRepository.deleteByTaskId(taskId);
    }

    public TaskLogger findOneByTaskId(Long taskId){
        return taskLoggerRepository.findByTaskId(taskId);
    }

}
