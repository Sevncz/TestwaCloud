package com.testwa.distest.server.service.user.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.UserMapper;
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
    public User findOne(Long key) {
        return userMapper.findOne(key);
    }

    @Override
    public List<User> findAll(List<Long> keys) {
        return userMapper.findList(keys);
    }

    @Override
    public List<User> findByUsernames(List<String> usernames) {
        return userMapper.findByUsernameList(usernames);
    }
    @Override
    public List<User> findByEmails(List<String> emails) {
        return userMapper.findByEmailList(emails);
    }

    @Override
    public List<User> query(User userPart) {
        return userMapper.query(userPart);
    }

    @Override
    public User getByCode(String userCode) {
        return userMapper.getByCode(userCode);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.getByEmail(email);
    }

    @Override
    public void updateActiveToTrue(String userCode) {
        userMapper.updateActiveToTrue(userCode);
    }

    @Override
    public void resetPwd(String userCode, String newHashPwd) {
        userMapper.resetPwd(userCode, newHashPwd);
    }
}