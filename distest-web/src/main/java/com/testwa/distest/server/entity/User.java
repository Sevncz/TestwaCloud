package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.common.enums.DB.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/8/27.
 */
@Data
@ToString
@Table(name="user")
public class User extends BaseEntity {
    @Column(name="userCode")
    private String userCode;
    @Column(name="email")
    private String email;
    @Column(name="mobile")
    private String mobile;
    @Column(name="username")
    private String username;
    @JsonIgnore
    @Column(name="password")
    private String password;
    @Column(name="sex")
    private Sex sex;
    @Column(name="nickname")
    private String nickname;
    @Column(name="address")
    private String address;
    @Column(name="country")
    private String country;
    @Column(name="header")
    private String header;
    @JsonIgnore
    @Column(name="lastPasswordResetTime")
    private Date lastPasswordResetTime;
    @Column(name="lastLoginTime")
    private Date lastLoginTime;
    @Column(name="loginTime")
    private Date loginTime;
    @Column(name="loginIp")
    private Integer loginIp;
    @Column(name="lastLoginIp")
    private Integer lastLoginIp;
    @JsonIgnore
    @Column(name="enabled")
    private Boolean enabled;
    @Column(name="registerTime")
    private Date registerTime;
    @JsonIgnore
    @Column(name="updateTime")
    private Date updateTime;
    @JsonIgnore
    @Transient
    private List<Role> roles;

    // 是否已激活
    @Column(name="isActive")
    private Boolean isActive;
    // 是否已激活
    @Column(name="isRealNameAuth")
    private Boolean isRealNameAuth;
    // 真实名字
    @Column(name="realName")
    private String realName;

}
