package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ISubTaskDAO extends IBaseDAO<SubTask, Long> {
    List<SubTask> findBy(SubTask entity);

    SubTask findOne(Long entityId);

    List<SubTask> findByTaskCode(Long taskCode);

    Long countBy(SubTask kq);

    SubTask findOne(Long taskCode, String deviceId);

    List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskId);

    void disableAll(Long taskCode);

    void updateVideoPath(long taskCode, String deviceId, String videoRelativePath);
}
