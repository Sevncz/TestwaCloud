package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TaskValidatoer {

    @Autowired
    private TaskService executionTaskService;


    public Task validateTaskExist(Long entityId) throws ObjectNotExistsException {
        Task exetask = executionTaskService.findOne(entityId);
        if(exetask == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return exetask;
    }


    public List<Task> validateTasksExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<Task> entityList = executionTaskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


}
