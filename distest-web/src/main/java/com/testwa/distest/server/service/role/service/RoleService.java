package com.testwa.distest.server.service.role.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.Role;
import com.testwa.distest.server.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Service
public class RoleService extends BaseService<Role, Long> {

    @Resource
    private RoleMapper roleMapper;

}
