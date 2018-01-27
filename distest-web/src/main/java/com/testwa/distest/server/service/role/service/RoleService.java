package com.testwa.distest.server.service.role.service;

import com.testwa.distest.server.service.role.dao.IRoleDAO;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Service
public class RoleService {

    @Resource
    private IRoleDAO roleDAO;

}
