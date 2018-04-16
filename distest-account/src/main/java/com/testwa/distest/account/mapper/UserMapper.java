package com.testwa.distest.account.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.account.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User, Long> {

	List<User> findBy(User user);

	User findOne(Long key);

    List<User> findList(List<Long> keys);

	List<User> findByUsernameList(List<String> usernameList);

	List<User> findByEmailList(List<String> emailList);

    List<User> query(User userPart);
}