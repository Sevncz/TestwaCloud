package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/8/27.
 */
@Data
@ToString
@TableName("user")
public class User extends BaseEntity {
    private String userCode;
    private String email;
    private String mobile;
    private String username;
    @JsonIgnore
    private String password;
    private Sex sex;
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

    // 是否已激活
    private Boolean isActive;
    // 是否已激活
    private Boolean isRealNameAuth;
    // 真实名字
    private String realName;

}
