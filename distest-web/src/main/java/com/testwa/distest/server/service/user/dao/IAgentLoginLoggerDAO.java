package com.testwa.distest.server.service.user.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.User;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAgentLoginLoggerDAO extends IBaseDAO<AgentLoginLogger, Long> {

    AgentLoginLogger findRecentLoginOne(String username);
}
