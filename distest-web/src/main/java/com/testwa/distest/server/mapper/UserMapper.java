package com.testwa.distest.server.mapper;

import java.util.List;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User, Long> {

	List<User> findBy(User user);

	User findOne(Long key);

    List<User> findList(List<Long> keys);

	List<User> findByUsernameList(List<String> usernameList);

	List<User> findByEmailList(List<String> emailList);

    List<User> query(User userPart);

    User getByCode(String userCode);

	void active(String userCode);
}