package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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
@TableName("user")
public class User extends BaseEntity {
    private String email;
    private String phone;
    private String username;
    private String password;
    private Sex sex;
    private String nickname;
    private String address;
    private String country;
    private String header;
    private Date lastPasswordResetTime;
    private Date lastLoginTime;
    private Date loginTime;
    private Integer loginIp;
    private Integer lastLoginIp;
    private Boolean enabled;
    private Date registerTime;
    private Date updateTime;
    private List<Role> roles;
}
