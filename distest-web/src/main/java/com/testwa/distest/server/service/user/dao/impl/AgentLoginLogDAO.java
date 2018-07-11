package com.testwa.distest.server.service.user.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.AgentLoginLog;
import com.testwa.distest.server.mapper.AgentLoginLogMapper;
import com.testwa.distest.server.service.user.dao.IAgentLoginLogDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class AgentLoginLogDAO extends BaseDAO<AgentLoginLog, Long> implements IAgentLoginLogDAO {

    @Resource
    private AgentLoginLogMapper agentLoginLoggerMapper;


    @Override
    public AgentLoginLog findRecentLoginOne(String username) {
        return agentLoginLoggerMapper.findRecentLoginOne(username);
    }
}