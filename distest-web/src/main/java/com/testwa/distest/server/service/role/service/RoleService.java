package com.testwa.distest.server.service.role.service;

import com.testwa.distest.server.service.role.dao.IRoleDAO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wen on 20/10/2017.
 */
@Service
public class RoleService {

    @Resource
    private IRoleDAO roleDAO;

}
