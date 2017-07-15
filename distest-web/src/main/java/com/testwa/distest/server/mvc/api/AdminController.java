package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.Validator;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.UserShareScope;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.model.message.ResultCode;
import com.testwa.distest.server.mvc.model.message.Result;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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


    @ResponseBody
    @RequestMapping(value = "create", method= RequestMethod.POST, produces={"application/json"})
    public Result create(@RequestBody final UserInfo register)
            throws ServletException {
        if(StringUtils.isBlank(register.password)
                || StringUtils.isBlank(register.username)
                || StringUtils.isBlank(register.email)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户名密码不能为空");
        }

        // 校验用户名
        if(!Validator.isUsername(register.username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户名格式不正确");
        }

        // 校验邮箱
        if(!Validator.isEmail(register.email)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "邮箱格式不正确");
        }

        User emailUser = userService.findByEmail(register.email);
        if(emailUser != null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "该邮箱已存在");
        }

        User usernameUser = userService.findByUsername(register.username);
        if(usernameUser != null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "该用户名已存在");
        }

        User awesomeUser = new User();
        awesomeUser.setEmail(register.email);
        awesomeUser.setPassword(passwordEncoder.encode(register.password));
        awesomeUser.setDateCreated(new Date());
        awesomeUser.setUsername(register.username);

        Result result = setScopeForUser(register.scope, awesomeUser);
        if (result != null) return result;

        userService.save(awesomeUser);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "modify/scope", method= RequestMethod.POST, produces={"application/json"})
    public Result modifyScope(@RequestBody Map<String, Object> params){

        String userId = (String) params.getOrDefault("userId", "");
        String scope = (String) params.getOrDefault("scope", "");
        if(StringUtils.isBlank(userId) || scope == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数不能为空");
        }
        User user = userService.findById(userId);
        if(user == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户不存在");
        }
        Result result = setScopeForUser(scope, user);
        if (result != null) return result;
        userService.save(user);
        return ok();
    }

    private Result setScopeForUser(String scope, User user) {
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
            return fail(ResultCode.PARAM_ERROR.getValue(), "Scope参数错误");
        }
        return null;
    }


    @ResponseBody
    @RequestMapping(value = "scope/all", method= RequestMethod.GET)
    public Result scopeAll(){
        Map<String, Object> result = new HashMap<>();
        result.put("scops", UserShareScope.values());
        return ok(result);
    }

}
