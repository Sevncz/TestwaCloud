package com.testwa.distest.server.web.auth.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.AccountException;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class UserValidator {

    @Autowired
    private UserService userService;
    @Autowired
    private User currentuUser;

    public void validateOnlineExist()  {
        User user = userService.findOne(currentuUser.getId());
        if(user == null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateEmailExist(String email)  {
        User user = userService.findByEmail(email);
        if(user == null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }
    public void validateEmailHasExist(String email) {
        User user = userService.findByEmail(email);
        if(user != null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "邮箱已存在");
        }
    }

    public void validateUsernamesExist(List<String> usernames)  {
        List<User> users = userService.findByUsernames(usernames);
        if(users == null || usernames.size() != users.size()){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateUsernameExist(String username)  {
        User user = userService.findByUsername(username);
        if(user == null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateUsernameHasExist(String username) {
        User user = userService.findByUsername(username);
        if(user != null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户名已存在");
        }
    }

    public void validateUserIdExist(Long userId)  {
        User user = userService.findOne(userId);
        if(user == null){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateUserIdsExist(List<Long> userIds) {
        List<User> users = userService.findByUserIds(userIds);
        if(users == null || userIds.size() != users.size()){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateUserCodesExist(List<String> userCodes) {
        List<User> users = userService.findByUserCodes(userCodes);
        if(users == null || userCodes.size() != users.size()){
            throw new AccountException(ResultCode.ILLEGAL_OP, "用户不存在");
        }
    }

    public void validateActive(String username){
        User user = userService.findByUsername(username);
        if(!user.getIsActive()) {
            throw new AccountException(ResultCode.ILLEGAL_OP, "账户未激活");
        }
    }
}
