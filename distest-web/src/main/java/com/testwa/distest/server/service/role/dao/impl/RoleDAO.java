package com.testwa.distest.server.service.role.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.Role;
import com.testwa.distest.server.mvc.mapper.RoleMapper;
import com.testwa.distest.server.service.role.dao.IRoleDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class RoleDAO extends BaseDAO<Role, Long> implements IRoleDAO {

    @Resource
    private RoleMapper roleMapper;


    public List<Role> findBy(Role role) {
        return roleMapper.findBy(role);
    }
}