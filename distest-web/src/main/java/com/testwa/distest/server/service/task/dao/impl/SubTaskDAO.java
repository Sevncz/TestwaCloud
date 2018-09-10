package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.mapper.SubTaskMapper;
import com.testwa.distest.server.service.task.dao.ISubTaskDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class SubTaskDAO extends BaseDAO<SubTask, Long> implements ISubTaskDAO {

    @Resource
    private SubTaskMapper mapper;

    public List<SubTask> findBy(SubTask entity) {
        return mapper.findBy(entity);
    }

    @Override
    public SubTask findOne(Long entityId) {
        return mapper.findOne(entityId);
    }

    @Override
    public List<SubTask> findByTaskCode(Long taskCode) {
        return mapper.findByTaskCode(taskCode);
    }

    @Override
    public Long countBy(SubTask query) {
        return mapper.countBy(query);
    }

    @Override
    public SubTask findOne(Long taskCode, String deviceId) {
        return mapper.findOneByTaskCodeAndDeviceId(taskCode, deviceId);
    }

    @Override
    public List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskCode) {
        return mapper.countTaskDeviceStatus(taskCode);
    }

    @Override
    public void disableAll(Long taskCode) {
        mapper.disableAll(taskCode);
    }

    @Override
    public void updateVideoPath(long taskCode, String deviceId, String videoRelativePath) {
        mapper.updateVideoPath(taskCode, deviceId, videoRelativePath);
    }

}