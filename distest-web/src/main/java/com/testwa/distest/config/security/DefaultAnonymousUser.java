package com.testwa.distest.config.security;

import com.testwa.distest.server.entity.User;

public class DefaultAnonymousUser extends User {

    public DefaultAnonymousUser() {
        this.setId(0L);
        this.setUsername("anonymous");
    }
}
