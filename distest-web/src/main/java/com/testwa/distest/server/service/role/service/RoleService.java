package com.testwa.distest.server.service.role.service;

import com.testwa.distest.server.service.role.dao.IRoleDAO;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Service
public class RoleService {

    @Resource
    private IRoleDAO roleDAO;

}
