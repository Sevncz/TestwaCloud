/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.dao.IAgentLoginLoggerDAO;
import com.testwa.distest.server.service.user.dao.IUserDAO;
import com.testwa.distest.server.service.user.form.RegisterForm;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AgentLoginLoggerService {

    @Autowired
    private IAgentLoginLoggerDAO agentLoginLoggerDAO;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(AgentLoginLogger entity) throws AccountException, AccountAlreadyExistException {
        return agentLoginLoggerDAO.insert(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateRecentLogoutTime(String username) {
        AgentLoginLogger logger = agentLoginLoggerDAO.findRecentLoginOne(username);
        if(logger != null){
            logger.setLogoutTime(new Date());
            agentLoginLoggerDAO.update(logger);
        }
    }
}
