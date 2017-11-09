package com.testwa.distest.server.mvc.mapper;

import java.util.List;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.User;

public interface UserMapper extends BaseMapper<User, Long> {

	List<User> findBy(User user);

	List<User> findByUsernameList(List<String> usernameList);

	List<User> findByEmailList(List<String> emailList);

}