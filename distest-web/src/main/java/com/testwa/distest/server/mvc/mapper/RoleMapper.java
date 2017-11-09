package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.Role;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role, Long> {

	List<Role> findBy(Role role);

}