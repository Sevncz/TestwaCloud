/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.distest.server.entity.AgentLoginLog;
import com.testwa.distest.server.service.user.dao.IAgentLoginLogDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AgentLoginLoggerService {

    @Autowired
    private IAgentLoginLogDAO agentLoginLoggerDAO;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(AgentLoginLog entity) throws AccountException, AccountAlreadyExistException {
        return agentLoginLoggerDAO.insert(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateRecentLogoutTime(String username) {
        AgentLoginLog logger = agentLoginLoggerDAO.findRecentLoginOne(username);
        if(logger != null){
            logger.setLogoutTime(new Date());
            agentLoginLoggerDAO.update(logger);
        }
    }
}
