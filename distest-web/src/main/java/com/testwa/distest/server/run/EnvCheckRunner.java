package com.testwa.distest.server.run;

import com.testwa.distest.server.model.ClientDetail;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.service.security.ClientDetailService;
import com.testwa.distest.server.service.security.RoleService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
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
    @Autowired
    private ClientDetailService clientDetailService;
    @Value("${app.client.id}")
    private String authClientId;
    @Value("${app.client.secret}")
    private String authClientSecret;


    @Override
    public void run(String... strings) throws Exception {
        List<ClientDetail> clientDetails = clientDetailService.listClientDetails();
        if (null == clientDetails) {
            clientDetailService.init();
        }else{
            try {
                clientDetailService.loadClientByClientId(authClientId);
            }catch (Exception e){
                clientDetailService.init();
            }

        }
        // role init
        roleService.init();

        // User init
        userService.initAdmin();
    }
}
