package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.AppiumFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentLoginLoggerMapper extends BaseMapper<AgentLoginLogger, Long> {

    AgentLoginLogger findRecentLoginOne(@Param("username") String username);

}