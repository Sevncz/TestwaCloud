package com.testwa.distest.server.service.user.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.AgentLoginLog;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAgentLoginLogDAO extends IBaseDAO<AgentLoginLog, Long> {

    AgentLoginLog findRecentLoginOne(String username);
}
