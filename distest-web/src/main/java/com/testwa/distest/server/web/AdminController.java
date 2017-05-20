package com.testwa.distest.server.web;

import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.permission.UserShareScope;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.util.Validator;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.ResultInfo;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 2016/11/12.
 */
@Api("管理模块相关api")
@RestController
@RequestMapping("admin")
public class AdminController extends BaseController{

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @SuppressWarnings("unused")
    private static class UserInfo {
        public String username;
        public String password;
        public String email;
        public String captcha;
        public String scope;
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "create", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> create(@RequestBody final UserInfo register)
            throws ServletException {
        if(StringUtils.isBlank(register.password)
                || StringUtils.isBlank(register.username)
                || StringUtils.isBlank(register.email)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名密码不能为空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 校验用户名
        if(!Validator.isUsername(register.username)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 校验邮箱
        if(!Validator.isEmail(register.email)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "邮箱格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User emailUser = userService.findByEmail(register.email);
        if(emailUser != null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "该邮箱已存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User usernameUser = userService.findByUsername(register.username);
        if(usernameUser != null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "该用户名已存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User awesomeUser = new User();
        awesomeUser.setEmail(register.email);
        awesomeUser.setPassword(passwordEncoder.encode(register.password));
        awesomeUser.setDateCreated(new Date());
        awesomeUser.setUsername(register.username);

        ResponseEntity<ResultInfo> result = setScopeForUser(register.scope, awesomeUser);
        if (result != null) return result;

        userService.save(awesomeUser);
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "modify/scope", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> modifyScope(@RequestBody Map<String, Object> params){

        String userId = (String) params.getOrDefault("userId", "");
        String scope = (String) params.getOrDefault("scope", "");
        if(StringUtils.isBlank(userId) || scope == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数不能为空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = userService.findById(userId);
        if(user == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ResponseEntity<ResultInfo> result = setScopeForUser(scope, user);
        if (result != null) return result;
        userService.save(user);
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    private ResponseEntity<ResultInfo> setScopeForUser(String scope, User user) {
        if(UserShareScope.contains(scope)){
            switch (UserShareScope.valueOf(scope)){
                case Self:
                    user.setShareScope(UserShareScope.Self.getValue());
                    break;
                case Project:
                    user.setShareScope(UserShareScope.Project.getValue());
                    break;
                case User:
                    user.setShareScope(UserShareScope.User.getValue());
                    break;
                case All:
                    user.setShareScope(UserShareScope.All.getValue());
                    break;
            }
        }else{
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "Scope参数错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "scope/all", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> scopeAll(){
        Map<String, Object> result = new HashMap<>();
        result.put("scops", UserShareScope.values());
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }

}
