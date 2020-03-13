package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.TaskEnv;
import com.testwa.distest.server.entity.TaskResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskEnvMapper extends BaseMapper<TaskEnv, Long> {

    TaskEnv getByTaskCodeAndDeviceId(@Param("taskCode") Long taskCode, @Param("deviceId") String deviceId);
}