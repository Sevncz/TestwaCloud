/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service.user.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.config.security.DefaultAnonymousUser;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.UserMapper;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class UserService extends BaseService<User, Long> {
    private static final String userCodePrefix = "U_";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SnowflakeIdWorker commonIdWorker;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRED)
    public String save(User user) {
        String userCode = userCodePrefix + commonIdWorker.nextId();
        user.setUserCode(userCode);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegisterTime(new Date());
        user.setEnabled(true);
        user.setIsActive(false);
        user.setIsRealNameAuth(false);
        userMapper.insert(user);
        return userCode;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAll(List<Long> ids) {
        ids.forEach( this::disable );
    }

    public User findByEmail(String email) {
        return userMapper.getByEmail(email);
    }

    public User findByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    public User findByUserCode(String userCode) {
        return userMapper.getByCode(userCode);
    }

    public List<User> findByUserCodes(List<String> userCodes) {
        return userMapper.findByUserCodeList(userCodes);
    }

    public List<User> findByUsernames(List<String> usernames) {
        List<User> users = userMapper.findByUsernameList(usernames);
        if(users.isEmpty()){
            return Collections.emptyList();
        }
        return users;
    }

    public long count() {
        return userMapper.count(null);
    }

    /**
     * 返回匹配的前十
     * @param form
     * @return
     */
    public List<User> queryUser(UserQueryForm form) {
        User query = new User();
        query.setUsername(form.getUsername());
        return userMapper.query(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateActiveToTrue(String userCode) {
        userMapper.updateActiveToTrue(userCode);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void resetPwd(String userCode, String newpassword) {
        String newHashPwd = passwordEncoder.encode(newpassword);
        userMapper.resetPwd(userCode, newHashPwd);
    }

    public List<User> findAll(List<Long> entityIds) {
        if(entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.findList(entityIds);
    }

    public User getCurrentUser() {
        return findByUsername(getCurrentUsername());
    }

    private String getCurrentUsername() {
        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            return new DefaultAnonymousUser().getUsername();
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof Principal) {
            return ((Principal) principal).getName();
        }
        return String.valueOf(principal);
    }
}
