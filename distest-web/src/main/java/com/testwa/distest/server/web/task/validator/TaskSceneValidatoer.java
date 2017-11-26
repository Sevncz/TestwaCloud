package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.TaskScene;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TaskSceneValidatoer {

    @Autowired
    private TaskSceneService taskService;


    public TaskScene validateTaskSceneExist(Long entityId) throws ObjectNotExistsException {
        TaskScene entity = taskService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entity;
    }


    public List<TaskScene> validateTaskScenesExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<TaskScene> entityList = taskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


}
