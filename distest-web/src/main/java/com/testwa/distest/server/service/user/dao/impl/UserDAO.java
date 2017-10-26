package com.testwa.distest.server.service.user.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.User;
import com.testwa.distest.server.mvc.mapper.UserMapper;
import com.testwa.distest.server.service.user.dao.IUserDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class UserDAO extends BaseDAO<User, Long> implements IUserDAO {

    @Resource
    private UserMapper userMapper;


    public List<User> findBy(User user) {
        return userMapper.findBy(user);
    }

    @Override
    public List<User> findByUsernames(List<String> usernames) {
        return userMapper.findByUsernameList(usernames);
    }
    @Override
    public List<User> findByEmails(List<String> emails) {
        return userMapper.findByEmailList(emails);
    }
}