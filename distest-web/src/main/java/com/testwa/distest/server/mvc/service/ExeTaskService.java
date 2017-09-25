package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.ExecutionTask;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.repository.ExecutionTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class ExeTaskService extends BaseService{

    @Autowired
    private ExecutionTaskRepository executionTaskRepository;

    public Page<ExecutionTask> findPage(PageRequest pageRequest, User user, String projectId) {
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where("projectId").is(projectId));
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);
        return executionTaskRepository.find(query, pageRequest);
    }



}
