package com.testwa.distest.config.security;

import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
public class CurrentUserConfig {
    @Autowired
    private UserService userService;

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Primary
    public User user() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new DefaultAnonymousUser();
        }
        return currentUser;
    }
}
