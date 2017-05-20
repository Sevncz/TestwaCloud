package com.testwa.distest.server.web;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.Validator;
import com.testwa.distest.server.authorization.Constants;
import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.permission.UserShareScope;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.service.security.TestwaTokenService;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.ResultInfo;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
    @Autowired
    private TestwaTokenService testwaTokenService;

    @RequestMapping(value = "login", method=RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> login(@RequestBody final UserInfo login,
                                            @ApiIgnore HttpServletRequest request,
                                            @ApiIgnore HttpServletResponse response)
        throws ServletException {
        Map<String, String> result = new HashMap<>();

        // 暂时取消验证码验证功能
//        if(StringUtils.isBlank(login.captcha)){
//            return new ResponseEntity<>(new CustomResponseEntity("验证码为空"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        String captchaKey = testwaTokenService.getCaptchaKey(request);
//        String captchaValue = (String) redisTemplate.opsForValue().get(captchaKey);
//        if (StringUtils.isBlank(captchaValue)){
//            log.error("captchaKey ============= {}", captchaKey);
//            return new ResponseEntity<>(new CustomResponseEntity("验证码已过期"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        redisTemplate.delete(captchaKey);
//        if (captchaValue.compareTo(login.captcha) != 0){
//            return new ResponseEntity<>(new CustomResponseEntity("非法的验证码"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }

        if (StringUtils.isBlank(login.username) || StringUtils.isBlank(login.password)) {
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名密码为空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = userService.findByUsername(login.username);
        if(user == null){
            user = userService.findByEmail(login.username);
        }
        if(user == null || !passwordEncoder.matches(login.password, user.getPassword())){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名密码错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String token = testwaTokenService.createToken(user.getId(), Constants.TOKEN_ISS, user.getUsername());
        result.put("access_token", token);
        testwaTokenService.saveToken(response, token);
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }


    @RequestMapping(value = "purelogin", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> pureLogin(@RequestBody final UserInfo login)
            throws ServletException {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isBlank(login.username) || StringUtils.isBlank(login.password)) {
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名密码为空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = userService.findByUsername(login.username);
        if(user == null){
            user = userService.findByEmail(login.username);
        }
        if(user == null || !passwordEncoder.matches(login.password, user.getPassword())){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名密码错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String token = testwaTokenService.createToken(user.getId(), Constants.TOKEN_ISS, user.getUsername());
        result.put("access_token", token);
        result.put("userId", user.getId());
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }

    @RequestMapping(value = "register", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> register(@RequestBody final UserInfo register)
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
        awesomeUser.setShareScope(UserShareScope.Self.getValue());
        userService.save(awesomeUser);
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @RequestMapping(value = "verify", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> verify(@ApiIgnore HttpServletRequest request){
        String token = testwaTokenService.getToken(request);
        if(StringUtils.isBlank(token)){
            return new ResponseEntity<>(errorInfo(ResultCode.NO_AUTH.getValue(), "Token信息为空"), HttpStatus.UNAUTHORIZED);
        }
        try {
            Claims claims = testwaTokenService.parserToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(errorInfo(ResultCode.NO_AUTH.getValue(), "用户登录信息已过期"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
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
        Cookie cookie = new Cookie(Constants.CAPTCHACODE, uuid);
        response.addCookie(cookie);
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
    public ResponseEntity<ResultInfo> logout(HttpServletRequest request,
                                         HttpServletResponse response){
        testwaTokenService.deleteToken(response);
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "verify/username/{username}", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> checkUsername(@PathVariable String username){
        // 校验用户名
        if(!Validator.isUsername(username)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户名格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User usernameUser = userService.findByUsername(username);
        if(usernameUser != null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "该用户名已存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "verify/email/{email}", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> checkEmail(@PathVariable String email){
        // 校验邮箱
        if(!Validator.isEmail(email)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "邮箱格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User usernameUser = userService.findByEmail(email);
        if(usernameUser != null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "该邮箱已存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/my/scopes", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> myScopes(@ApiIgnore @CurrentUser User user){
        Integer scope = user.getShareScope();
        List<UserShareScope> scopes = UserShareScope.lteScope(scope);
        Map<String, Object> result = new HashMap<>();
        result.put("scops", scopes);
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }

}