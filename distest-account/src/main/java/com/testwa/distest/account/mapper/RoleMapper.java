package com.testwa.distest.account.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.account.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMapper extends BaseMapper<Role, Long> {

	List<Role> findBy(Role role);

}