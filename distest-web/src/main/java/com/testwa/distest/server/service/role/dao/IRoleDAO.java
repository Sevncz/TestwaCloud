package com.testwa.distest.server.service.role.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.core.entity.Role;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IRoleDAO extends IBaseDAO<Role, Long> {
    List<Role> findBy(Role role);
}
