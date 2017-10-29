package com.testwa.distest.server.web.task.validator;

import com.testwa.core.entity.TaskScene;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TaskValidatoer {

    @Autowired
    private TaskSceneService taskService;


    public TaskScene validateTaskExist(Long entityId) throws ObjectNotExistsException {
        TaskScene task = taskService.findOne(entityId);
        if(task == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return task;
    }


    public List<TaskScene> validateTasksExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<TaskScene> entityList = taskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


}
