package com.testwa.distest.server.mvc.repository;

import com.testwa.core.Command;
import com.testwa.distest.server.mvc.model.ExecutionTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
