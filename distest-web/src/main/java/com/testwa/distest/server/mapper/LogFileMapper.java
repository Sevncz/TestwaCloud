package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.LogFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogFileMapper extends BaseMapper<LogFile, Long> {


    LogFile findOne(@Param("taskCode") Long taskId, @Param("deviceId") String deviceId);

    List<LogFile> fildAll(LogFile query);

    int removeFromTask(Long taskId);

}