package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.Role;
import com.testwa.core.entity.User;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role, Long> {

	List<Role> findBy(Role role);

}