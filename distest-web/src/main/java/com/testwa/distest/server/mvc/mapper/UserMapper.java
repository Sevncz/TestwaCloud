package com.testwa.distest.server.mvc.mapper;

import java.util.List;
import java.util.Map;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

public interface UserMapper extends BaseMapper<User, Long> {

	List<User> findBy(User user);

	List<User> findByUsernameList(List<String> usernameList);

	List<User> findByEmailList(List<String> emailList);

}