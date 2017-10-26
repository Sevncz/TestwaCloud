package com.testwa.distest.server.mvc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface ExecutionTaskRepository extends CommonRepository<ExecutionTask, Serializable> {

    Page<ExecutionTask> findByProjectIdAndCreatorAndStatusIn(String projectId, String id, List<Integer> notFinishedCode, Pageable pageable);

    Page<ExecutionTask> findByProjectIdAndCreatorAndStatusNotIn(String projectId, String userId, List<Integer> finishedCode, Pageable pageable);
}
