package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Task;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TaskRepository extends CommonRepository<Task, Serializable> {

    List<Task> findByProjectId(String projectId);

    Integer countByProjectId(String projectId);
}
