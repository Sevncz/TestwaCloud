package com.testwa.distest.server.service.task.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.mvc.entity.Task;
import com.testwa.distest.server.mvc.entity.TaskTestcase;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskTestcaseDAO extends IBaseDAO<TaskTestcase, Long> {
    List<TaskTestcase> findBy(TaskTestcase entity);

    void insertAll(List<TaskTestcase> entityList);
}
