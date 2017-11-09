/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.distest.common.exception.AccountAlreadyExistException;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.dao.IUserDAO;
import com.testwa.distest.server.service.user.form.RegisterForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public long save(RegisterForm form) throws AccountException, AccountAlreadyExistException {
        User newUser = new User();
        newUser.setEmail(form.email);
        newUser.setPassword(form.password);
        newUser.setUsername(form.username);
        return save(newUser);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public long save(User user) throws AccountAlreadyExistException, AccountException {
        String username = user.getUsername();
        String email = user.getEmail();

        User emailUser = this.findByEmail(email);
        if(emailUser != null){
            throw new AccountAlreadyExistException("该邮箱已存在");
        }

        User usernameUser = this.findByUsername(username);
        if(usernameUser != null){
            throw new AccountAlreadyExistException("该用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegisterTime(new Date());
        return userDAO.insert(user);
    }

    /**
     * 更新对象
     * @param user
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public int update(User user) {
        return userDAO.update(user);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public int delete(Integer id) {
        return userDAO.delete(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public int deleteAll(List<Long> idList) {
        return userDAO.delete(idList);
    }

    public User findByEmail(String email) {
        User query = new User();
        query.setEmail(email);
        List<User> users = userDAO.findBy(query);
        if(users.size() > 1){
            log.error("find by email return > 1");
//            throw new AccountException("账户异常");
        }
        if(users.size() == 0){
            return null;
        }
        return users.get(0);
    }

    public User findByUsername(String username) {
        User query = new User();
        query.setUsername(username);
        List<User> users = userDAO.findBy(query);
        if(users.size() > 1){
            log.error("find by username return > 1");
//            throw new AccountException("账户异常");
        }
        if(users.size() == 0){
            return null;
        }
        return users.get(0);
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
}
