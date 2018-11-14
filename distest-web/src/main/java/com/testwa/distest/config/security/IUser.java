package com.testwa.distest.config.security;

public interface IUser {
    String getName();

    String getMobile();

    Integer getId();

    Integer getVisible();

    Role getRole();

    String getAvatar();
    // 角色枚举
    enum Role {
        // 普通用户，企业用户。
        USER, COMPANY, ALL, NONE;
    }
}