/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.Role;
import com.testwa.distest.server.mvc.repository.RoleRepository;
import com.testwa.distest.server.mvc.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(String userId) {
        assert StringUtils.isBlank(userId);
        return userRepository.findById(userId);
    }

    /**
     * 更新对象
     * @param user
     */
    public void update(User user) {
        Query query=new Query(Criteria.where("id").is(user.getId()));
        Update update= new Update();
        for(Field f : User.class.getDeclaredFields()){
            try {
                f.setAccessible(true);
                update.set(f.getName(), f.get(user));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        userRepository.updateInser(query, update);
    }


    public void initAdmin() {

        User user = findByEmail("admin@testwa.com");
        if(null == user){
            User awesomeUser = new User();
            awesomeUser.setEmail("admin@testwa.com");
            awesomeUser.setPassword(passwordEncoder.encode("admin"));
            awesomeUser.setDateCreated(new Date());
            awesomeUser.setPhone("18600753024");
            awesomeUser.setUsername("admin");
            awesomeUser.setEnabled(true);
            awesomeUser.setLastPasswordResetDate(new Date());

            Role role = roleRepository.findByValue("admin");
            if(role == null){
                log.error("role admin is null");
            }else{
                List<String> roles = new ArrayList<>();
                roles.add(role.getValue());
                awesomeUser.setRoles(roles);
                save(awesomeUser);
            }
        }
    }

    public List<User> findByUserIds(List<String> userIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(userIds));
        return userRepository.find(query);

    }
}
