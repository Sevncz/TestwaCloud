/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.AgentLoginLog;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.mapper.AgentLoginLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AgentLoginLoggerService extends BaseService<AgentLoginLog, Long> {

    @Autowired
    private AgentLoginLogMapper agentLoginLogMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRecentLogoutTime(String username) {
        AgentLoginLog logger = agentLoginLogMapper.findRecentLoginOne(username);
        if(logger != null){
            logger.setLogoutTime(new Date());
            agentLoginLogMapper.update(logger);
        }
    }
}
