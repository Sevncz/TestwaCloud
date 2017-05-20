/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaTestcase;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.permission.Role;
import com.testwa.distest.server.repository.RoleRepository;
import com.testwa.distest.server.repository.UserRepository;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private Environment env;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(String id) {
        return userRepository.findOne(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return userRepository.find(query, pageRequest);
    }

    public void initAdmin() {

        User user = findByEmail("admin@testwa.com");
        if(null == user){
            User awesomeUser = new User();
            awesomeUser.setEmail("admin@testwa.com");
            awesomeUser.setPassword(passwordEncoder.encode("admin"));
            awesomeUser.setId("thisis-awesome-1");
            awesomeUser.setDateCreated(new Date());
            awesomeUser.setUsername("admin");

            Role role = roleRepository.findByValue("admin");
            if(role == null){
                log.error("role admin is null");
            }else{
                awesomeUser.setRoleCode(role.getCode());
                save(awesomeUser);
            }
        }
    }

    public User findById(String userId) {
        assert StringUtils.isBlank(userId);
        return userRepository.findById(userId);
    }
}
