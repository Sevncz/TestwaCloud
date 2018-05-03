package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.TaskDevice;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskDeviceDAO extends IBaseDAO<TaskDevice, Long> {
    List<TaskDevice> findBy(TaskDevice entity);

    TaskDevice findOne(Long entityId);

    List<TaskDevice> findByTaskId(Long taskId);

    Long countBy(TaskDevice kq);

    TaskDevice findOne(Long taskId, String deviceId);

    List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskId);

    void deleteByTaskId(Long taskId);
}
