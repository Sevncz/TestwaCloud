package com.testwa.distest.server.service.security;

import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.permission.Role;
import com.testwa.distest.server.repository.RoleRepository;
import com.testwa.distest.server.repository.UserRepository;
import com.testwa.distest.server.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 2016/11/12.
 */
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

    public Role findByCode(Integer code){
        return roleRepository.findByCode(code);
    }

    public void init() {
        Role admin = new Role("管理员", 0, "admin");
        if(findByValue(admin.getValue()) == null){
            save(admin);
        }
        Role user = save(new Role("普通用户", 1, "user"));
        if(findByValue(user.getValue()) == null){
            save(user);
        }

    }
}
