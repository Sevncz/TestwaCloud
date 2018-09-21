package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskDAO extends IBaseDAO<Task, Long> {
    List<Task> findBy(Task entity);

    Task findOne(Long entityId);

    Long countBy(Task kq);

    Task findByCode(Long taskCode);

    void disableAll(List<Long> taskCodes);

    void finish(Long taskCode, Date endTime, DB.TaskStatus status);

    List<Task> findFinishBy(Task query);

    List<Task> findFinishBy(Task query, Long startTime, Long endTime);

    List<CountAppTestStatisDTO> countAppTest(Long projectId, Long startTime, Long endTime);

    List<CountMemberTestStatisDTO> countMemberTest(Long projectId, Long startTime, Long endTime);
}
