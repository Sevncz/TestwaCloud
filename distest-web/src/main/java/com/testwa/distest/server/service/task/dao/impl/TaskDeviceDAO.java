package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.TaskDevice;
import com.testwa.distest.server.mapper.TaskDeviceMapper;
import com.testwa.distest.server.service.task.dao.ITaskDeviceDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class TaskDeviceDAO extends BaseDAO<TaskDevice, Long> implements ITaskDeviceDAO {

    @Resource
    private TaskDeviceMapper mapper;

    public List<TaskDevice> findBy(TaskDevice entity) {
        return mapper.findBy(entity);
    }

    @Override
    public TaskDevice findOne(Long entityId) {
        return mapper.findOne(entityId);
    }

    @Override
    public List<TaskDevice> findByTaskId(Long taskId) {
        return mapper.findByTaskId(taskId);
    }

    @Override
    public Long countBy(TaskDevice query) {
        return mapper.countBy(query);
    }

    @Override
    public TaskDevice findOne(Long taskId, String deviceId) {
        return mapper.findOneByTaskIdAndDeviceId(taskId, deviceId);
    }

    @Override
    public List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskId) {
        return mapper.countTaskDeviceStatus(taskId);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        mapper.deleteByTaskId(taskId);
    }

}