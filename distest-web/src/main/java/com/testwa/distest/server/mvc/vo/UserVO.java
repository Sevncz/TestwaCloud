package com.testwa.distest.server.mvc.vo;

import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.User;

/**
 * Created by wen on 29/07/2017.
 */
public class UserVO {
    private String userId;
    private String username;
    private String email;

    public UserVO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
