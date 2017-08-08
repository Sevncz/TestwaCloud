package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.Role;
import com.testwa.distest.server.mvc.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends BaseService {

    @Autowired
    private RoleRepository roleRepository;

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Role findByValue(String value) {
        return roleRepository.findByValue(value);
    }

    public Role findById(String id){
        return roleRepository.findById(id);
    }

    public void init() {
        Role admin = new Role("管理员", "admin");
        if(findByValue(admin.getValue()) == null){
            save(admin);
        }
        Role user = new Role("普通用户", "user");
        if(findByValue(user.getValue()) == null){
            save(user);
        }

    }
}