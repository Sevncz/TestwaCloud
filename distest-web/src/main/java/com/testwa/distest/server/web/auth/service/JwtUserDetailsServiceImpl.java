package com.testwa.distest.server.web.auth.service;

import com.testwa.core.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.dto.JwtUserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("No auth found with username '%s'.", username));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}
