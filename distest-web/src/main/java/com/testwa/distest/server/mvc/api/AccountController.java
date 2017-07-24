package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.Validator;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.UserShareScope;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
import io.swagger.annotations.Api;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Api("用户账号相关api")
@RestController
@RequestMapping("account")
public class AccountController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(value = "register", method= RequestMethod.POST, produces={"application/json"})
    public Result register(@RequestBody final UserInfo register)
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
        awesomeUser.setShareScope(UserShareScope.Self.getValue());
        userService.save(awesomeUser);
        return ok();
    }

    @SuppressWarnings("unused")
    private static class UserInfo {
        public String username;
        public String password;
        public String email;
        public String captcha;
    }

    private static int captchaExpires = 3*60; //超时时间3min
    private static int captchaW = 200;
    private static int captchaH = 60;

    @RequestMapping(value = "captcha", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getCaptcha(@ApiIgnore HttpServletResponse response){
        //生成验证码
        String uuid = Identities.uuid();
        Captcha captcha = new Captcha.Builder(captchaW, captchaH)
                .addText().addBackground(new GradiatedBackgroundProducer())
                .gimp(new FishEyeGimpyRenderer())
                .build();

        //将验证码以<key,value>形式缓存到redis
        redisTemplate.opsForValue().set(uuid, captcha.getAnswer(), captchaExpires, TimeUnit.SECONDS);

        //将验证码key，及验证码的图片返回
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            ImageIO.write(captcha.getImage(), "png", bao);
            return bao.toByteArray();
        } catch (IOException e) {
            log.error("Write captcha error", e);
            return null;
        }
    }

    @RequestMapping(value = "logout", method= RequestMethod.POST, produces={"application/json"})
    public Result logout(HttpServletRequest request,
                                         HttpServletResponse response){
        return ok();
    }

    @RequestMapping(value = "verify/username/{username}", method= RequestMethod.GET)
    public Result checkUsername(@PathVariable String username){
        // 校验用户名
        if(!Validator.isUsername(username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户名格式不正确");
        }
        User usernameUser = userService.findByUsername(username);
        if(usernameUser != null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "该用户名已存在");
        }
        return ok();
    }

    @RequestMapping(value = "verify/email/{email}", method= RequestMethod.GET)
    public Result checkEmail(@PathVariable String email){
        // 校验邮箱
        if(!Validator.isEmail(email)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "邮箱格式不正确");
        }
        User usernameUser = userService.findByEmail(email);
        if(usernameUser != null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "该邮箱已存在");
        }
        return ok();
    }



    @ResponseBody
    @RequestMapping(value = "/my/scopes", method= RequestMethod.GET)
    public Result myScopes(){
        return ok();
    }

}