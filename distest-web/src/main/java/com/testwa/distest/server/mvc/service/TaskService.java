package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.Task;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.repository.TaskRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TaskService extends BaseService{

    @Autowired
    private TaskRepository taskRepository;

    public void save(Task task){
        taskRepository.save(task);
    }

    public void deleteById(String taskId){
        taskRepository.delete(taskId);
    }

    public Page<Task> findAll(PageRequest pageRequest) {
        return taskRepository.findAll(pageRequest);
    }

    public Page<Task> findPage(PageRequest pageRequest, String appId, List<String> projectIds) {
        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(appId)){
            andCriteria.add(Criteria.where("appId").is(appId));
        }
        andCriteria.add(Criteria.where("projectId").in(projectIds));
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);
        return taskRepository.find(query, pageRequest);
    }

}
