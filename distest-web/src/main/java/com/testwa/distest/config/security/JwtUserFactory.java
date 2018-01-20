package com.testwa.distest.config.security;

import com.testwa.distest.server.entity.Role;
import com.testwa.distest.server.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;


final class JwtUserFactory {

    private JwtUserFactory() {
    }

    static JwtUser create(User user) {
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                mapToGrantedAuthorities(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList())),
                user.getLastPasswordResetTime()
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
