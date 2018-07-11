package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AgentLoginLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentLoginLogMapper extends BaseMapper<AgentLoginLog, Long> {

    AgentLoginLog findRecentLoginOne(@Param("username") String username);

}