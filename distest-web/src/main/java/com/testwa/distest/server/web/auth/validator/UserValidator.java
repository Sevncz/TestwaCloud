package com.testwa.distest.server.web.auth.validator;

import com.testwa.core.base.exception.AccountException;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class UserValidator {

    @Autowired
    private UserService userService;

    public void validateOnlineExist() throws AccountException {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        if(user == null){
            throw new AccountException("user.not.exist");
        }
    }

    public void validateEmailExist(String email) throws AccountException {
        User user = userService.findByEmail(email);
        if(user == null){
            throw new AccountException("user.not.exist");
        }
    }

    public void validateUsernamesExist(List<String> usernames) throws AccountNotFoundException {
        List<User> users = userService.findByUsernames(usernames);
        if(users == null || usernames.size() != users.size()){
            throw new AccountNotFoundException();
        }
    }

    public void validateUsernameExist(String username) throws AccountNotFoundException {
        User user = userService.findByUsername(username);
        if(user == null){
            throw new AccountNotFoundException();
        }
    }

}
