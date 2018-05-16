package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.LoggerFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoggerFileMapper extends BaseMapper<LoggerFile, Long> {


    LoggerFile findOne(@Param("taskId") Long taskId, @Param("deviceId") String deviceId);

    List<LoggerFile> fildAll(LoggerFile query);

    int removeFromTask(Long taskId);

}