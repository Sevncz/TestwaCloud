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

    List<TaskDevice> findByTaskCode(@Param("taskCode") Long taskCode);

    TaskDevice findOne(Long key);

    Long countBy(TaskDevice query);

    TaskDevice findOneByTaskCodeAndDeviceId(@Param("taskCode") Long taskCode, @Param("deviceId") String deviceId);

    List<TaskDeviceStatusStatis> countTaskDeviceStatus(@Param("taskCode") Long taskId);

    void disableAll(@Param("taskCode") Long taskId);

    void updateVideoPath(@Param("taskCode") Long taskCode, @Param("deviceId") String deviceId, @Param("videoPath") String videoPath);
}