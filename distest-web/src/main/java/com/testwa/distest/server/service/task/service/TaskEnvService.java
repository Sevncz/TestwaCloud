package com.testwa.distest.server.service.task.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.TaskEnv;
import com.testwa.distest.server.entity.TaskResult;
import com.testwa.distest.server.mapper.TaskEnvMapper;
import com.testwa.distest.server.mapper.TaskResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskEnvService extends BaseService<TaskEnv, Long> {

    @Autowired
    private TaskEnvMapper taskEnvMapper;

    public List<TaskEnv> listByCode(Long taskCode) {
        return taskEnvMapper.selectListByProperty(TaskEnv::getTaskCode, taskCode);
    }

    public TaskEnv getByTaskCodeAndDeviceId(Long taskCode, String deviceId) {
        return taskEnvMapper.getByTaskCodeAndDeviceId(taskCode, deviceId);
    }
}
