package com.testwa.distest.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.account.common.enums.DB;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/8/27.
 */
@Data
@ToString
@TableName("user")
public class User extends BaseEntity {
    private String email;
    private String phone;
    private String username;
    @JsonIgnore
    private String password;
    private DB.Sex sex;
    private String nickname;
    private String address;
    private String country;
    private String header;
    @JsonIgnore
    private Date lastPasswordResetTime;
    private Date lastLoginTime;
    private Date loginTime;
    private Integer loginIp;
    private Integer lastLoginIp;
    @JsonIgnore
    private Boolean enabled;
    private Date registerTime;
    @JsonIgnore
    private Date updateTime;
    @JsonIgnore
    @Column(value = "role", ignore = true)
    private List<Role> roles;

}
