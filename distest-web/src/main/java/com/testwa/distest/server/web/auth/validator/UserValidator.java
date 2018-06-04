package com.testwa.distest.server.web.auth.validator;

import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.AccountNoActiveException;
import com.testwa.core.base.exception.ObjectAlreadyExistException;
import com.testwa.core.base.exception.ObjectNotExistsException;
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

    public void validateOnlineExist() throws ObjectNotExistsException {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        if(user == null){
            throw new ObjectNotExistsException("用户不存在");
        }
    }

    public void validateEmailExist(String email) throws ObjectNotExistsException {
        User user = userService.findByEmail(email);
        if(user == null){
            throw new ObjectNotExistsException("用户不存在");
        }
    }
    public void validateEmailHasExist(String email) throws ObjectAlreadyExistException {
        User user = userService.findByEmail(email);
        if(user != null){
            throw new ObjectAlreadyExistException("邮箱已存在");
        }
    }

    public void validateUsernamesExist(List<String> usernames) throws ObjectNotExistsException {
        List<User> users = userService.findByUsernames(usernames);
        if(users == null || usernames.size() != users.size()){
            throw new ObjectNotExistsException("用户不存在");
        }
    }

    public void validateUsernameExist(String username) throws ObjectNotExistsException {
        User user = userService.findByUsername(username);
        if(user == null){
            throw new ObjectNotExistsException("用户不存在");
        }
    }

    public void validateUsernameHasExist(String username) throws ObjectAlreadyExistException {
        User user = userService.findByUsername(username);
        if(user != null){
            throw new ObjectAlreadyExistException("用户名已存在");
        }
    }

    public void validateUserIdExist(Long userId) throws ObjectNotExistsException {
        User user = userService.findOne(userId);
        if(user == null){
            throw new ObjectNotExistsException("用户不存在");
        }
    }
    public void validateUserIdsExist(List<Long> userIds) throws AccountNotFoundException {
        List<User> users = userService.findByUserIds(userIds);
        if(users == null || userIds.size() != users.size()){
            throw new AccountNotFoundException("用户不存在");
        }
    }

    public void validateActive(String username) throws AccountNoActiveException{
        User user = userService.findByUsername(username);
        if(!user.getIsActive()) {
            throw new AccountNoActiveException("账户未激活");
        }
    }
}
