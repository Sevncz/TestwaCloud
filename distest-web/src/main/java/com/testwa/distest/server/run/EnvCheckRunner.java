package com.testwa.distest.server.run;

import com.testwa.distest.server.service.RoleService;
import com.testwa.distest.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 16/9/18.
 */
@Component
public class EnvCheckRunner implements CommandLineRunner {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Value("${app.client.id}")
    private String authClientId;
    @Value("${app.client.secret}")
    private String authClientSecret;


    @Override
    public void run(String... strings) throws Exception {
        // role init
        roleService.init();

        // User init
        userService.initAdmin();
    }
}
