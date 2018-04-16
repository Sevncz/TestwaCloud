/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.account.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.account.dao.IUserDAO;
import com.testwa.distest.account.entity.User;
import com.testwa.distest.account.form.RegisterForm;
import com.testwa.distest.account.form.UserQueryForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class UserService {

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(RegisterForm form) throws AccountException, AccountAlreadyExistException {
        User newUser = new User();
        newUser.setEmail(form.email);
        newUser.setPassword(form.password);
        newUser.setUsername(form.username);
        return save(newUser);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
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
        user.setEnabled(true);
        return userDAO.insert(user);
    }

    /**
     * 更新对象
     * @param user
     */
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
        User query = new User();
        query.setEmail(email);
        List<User> users = userDAO.findBy(query);
        if(users.size() > 1){
            log.error("findForCurrentUser by email return > 1, {}", email);
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
            log.error("findForCurrentUser by username return > 1, {}", username);
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
}
