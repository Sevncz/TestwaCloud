/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.dao.IUserDAO;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class UserService {
    private static final String userCodePrefix = "U_";
    @Autowired
    private IUserDAO userDAO;
    @Autowired
    private SnowflakeIdWorker commonIdWorker;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public String save(User user) throws AccountAlreadyExistException, AccountException {
        String userCode = userCodePrefix + commonIdWorker.nextId();
        user.setUserCode(userCode);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegisterTime(new Date());
        user.setEnabled(true);
        user.setIsActive(false);
        user.setIsRealNameAuth(false);
        userDAO.insert(user);
        return userCode;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int update(User user) {
        return userDAO.update(user);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(Integer id) {
        return userDAO.delete(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int deleteAll(List<Long> idList) {
        return userDAO.delete(idList);
    }

    public User findByEmail(String email) {
        return userDAO.getByEmail(email);
    }

    public User findByUsername(String username) {
        return userDAO.getByUsername(username);
    }

    public User findByUserCode(String userCode) {
        return userDAO.getByCode(userCode);
    }

    public List<User> findByUsernames(List<String> usernames) {
        List<User> users = userDAO.findByUsernames(usernames);
        if(users.size() == 0){
            return null;
        }
        return users;
    }

    public User findOne(Long userId) {
        return userDAO.findOne(userId);
    }

    public List<User> findByUserIds(List<Long> userIds) {
        return userDAO.findAll(userIds);
    }

    public long count() {
        return userDAO.count();
    }

    public PageResult<User> findByPage(User user, int page, int rows){
        //分页处理
        PageHelper.startPage(page, rows);
        List<User> userList = userDAO.findBy(user);
        PageInfo info = new PageInfo(userList);
        PageResult<User> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    /**
     * 返回匹配的前十
     * @param form
     * @return
     */
    public List<User> queryUser(UserQueryForm form) {
        User query = new User();
        query.setUsername(form.getUsername());
        return userDAO.query(query);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateActiveToTrue(String userCode) {
        userDAO.updateActiveToTrue(userCode);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void resetPwd(String userCode, String newpassword) {
        String newHashPwd = passwordEncoder.encode(newpassword);
        userDAO.resetPwd(userCode, newHashPwd);
    }

    public List<User> findAll(List<Long> entityIds) {
        return userDAO.findAll(entityIds);
    }
}
