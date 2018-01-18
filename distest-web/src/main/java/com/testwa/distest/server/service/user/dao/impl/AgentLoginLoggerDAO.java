package com.testwa.distest.server.service.user.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.AgentLoginLoggerMapper;
import com.testwa.distest.server.mapper.UserMapper;
import com.testwa.distest.server.service.user.dao.IAgentLoginLoggerDAO;
import com.testwa.distest.server.service.user.dao.IUserDAO;
import org.springframework.stereotype.Repository;
import sun.management.Agent;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class AgentLoginLoggerDAO extends BaseDAO<AgentLoginLogger, Long> implements IAgentLoginLoggerDAO {

    @Resource
    private AgentLoginLoggerMapper agentLoginLoggerMapper;


    @Override
    public AgentLoginLogger findRecentLoginOne(String username) {
        return agentLoginLoggerMapper.findRecentLoginOne(username);
    }
}