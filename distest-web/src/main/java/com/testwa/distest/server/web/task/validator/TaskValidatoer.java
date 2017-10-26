package com.testwa.distest.server.web.task.validator;

import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.entity.Script;
import com.testwa.distest.server.mvc.entity.Task;
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
    private TaskService taskService;


    public Task validateTaskExist(Long entityId) throws ObjectNotExistsException {
        Task task = taskService.findOne(entityId);
        if(task == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return task;
    }


    public List<Task> validateTasksExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<Task> entityList = taskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


}
