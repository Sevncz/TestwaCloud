package com.testwa.distest.server.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/8/27.
 */
@Document(collection = "t_user")
public class User {
    @Id
    private String id;
    @Size(min = 4, max = 50)
    private String email;
    @Indexed(unique = true)
    @Size(min = 11, max = 11)
    private String phone;
    @Indexed(unique = true)
    private String username;
    private String password;
    private List<String> rights;
    @CreatedDate
    private Date dateCreated;
    // 拥有设备可分享范围的权限
    private Integer shareScope;

    // 角色列表
    private List<String> roles;
    // 上次修改密码时间
    private Date lastPasswordResetDate;
    // 已使用过的密码
    private List<String> oldPasswordList;
    private Date lastLoginTime;
    private Date loginTime;
    private Integer loginIp;
    private Integer lastLoginIp;

    private Boolean enabled;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<String> getRights() {
        return rights;
    }

    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getShareScope() {
        return shareScope;
    }

    public void setShareScope(Integer shareScope) {
        this.shareScope = shareScope;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Date lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Integer getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(Integer loginIp) {
        this.loginIp = loginIp;
    }

    public Integer getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(Integer lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public List<String> getOldPasswordList() {
        return oldPasswordList;
    }

    public void setOldPasswordList(List<String> oldPasswordList) {
        this.oldPasswordList = oldPasswordList;
    }
}
