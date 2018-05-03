package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.TaskDevice;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskDeviceMapper extends BaseMapper<Task, Long> {

    List<TaskDevice> findBy(TaskDevice entity);

    List<TaskDevice> findByTaskId(@Param("taskId") Long taskId);

    TaskDevice findOne(Long key);

    Long countBy(TaskDevice query);

    TaskDevice findOneByTaskIdAndDeviceId(@Param("taskId") Long taskId, @Param("deviceId") String deviceId);

    List<TaskDeviceStatusStatis> countTaskDeviceStatus(@Param("taskId") Long taskId);

    void deleteByTaskId(@Param("taskId") Long taskId);
}