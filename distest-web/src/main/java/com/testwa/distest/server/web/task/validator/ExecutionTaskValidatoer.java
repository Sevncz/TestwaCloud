package com.testwa.distest.server.web.task.validator;

import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.entity.ExecutionTask;
import com.testwa.distest.server.service.task.service.ExecutionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class ExecutionTaskValidatoer {

    @Autowired
    private ExecutionTaskService executionTaskService;


    public ExecutionTask validateExecutionTaskExist(Long entityId) throws ObjectNotExistsException {
        ExecutionTask exetask = executionTaskService.findOne(entityId);
        if(exetask == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return exetask;
    }


    public List<ExecutionTask> validateExecutionTasksExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<ExecutionTask> entityList = executionTaskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


}
