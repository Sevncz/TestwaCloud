package com.testwa.distest.server.web.auth.controller;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.Result;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.Validator;
import com.testwa.distest.config.security.JwtAuthenticationRequest;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
import com.testwa.distest.exception.AccountException;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.form.RegisterForm;
import com.testwa.distest.server.service.user.form.ResetPasswordForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.mgr.AuthMgr;
import com.testwa.distest.server.web.auth.mgr.RedisLoginMgr;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("登录注册相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/auth")
public class AuthController extends BaseController {
    private static final int CAPTCHA_W = 200;
    private static final int CAPTCHA_H = 60;

    @Value("${jwt.header}")
    private String tokenHeader;
    @Autowired
    private UserService userService;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private AuthMgr authMgr;
    @Autowired
    private RedisLoginMgr redisLoginMgr;
    @Autowired
    private User currentUser;

    @ApiOperation(value = "登录")
    @ApiImplicitParam(name = "authenticationRequest", value = "JWT登录验证类", required = true, dataType = "JwtAuthenticationRequest")
    @PostMapping(value = "login")
    public JwtAuthenticationResponse createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletRequest request) {
        if(StringUtils.isEmpty(authenticationRequest.getUsername()) || StringUtils.isEmpty(authenticationRequest.getPassword())){
            throw new AccountException(ResultCode.INVALID_PARAM, "登录信息不能为空");
        }
        String ip;
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        }else{
            ip = request.getHeader("x-forwarded-for");
        }
        userValidator.validateUsernameExist(authenticationRequest.getUsername());
        String userAgent = request.getHeader("user-agent");
        userValidator.validateActive(authenticationRequest.getUsername());
        return authMgr.login(authenticationRequest.getUsername(), authenticationRequest.getPassword(), ip, userAgent);
    }


    @ApiOperation(value = "刷新Token")
    @ApiImplicitParam(name = "request", value = "请求信息（带有tokenHeader）", required = true, dataType = "HttpServletRequest")
    @GetMapping(value = "/refresh")
    public JwtAuthenticationResponse refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        return authMgr.refresh(token);
    }

    @ApiOperation(value = "注册")
    @ApiImplicitParam(name = "form", value = "注册form", required = true, dataType = "RegisterForm")
    @PostMapping(value = "/register")
    public void register(@RequestBody @Valid final RegisterForm form) {
        userValidator.validateEmailHasExist(form.getEmail());
        userValidator.validateUsernameHasExist(form.getUsername());

        User newUser = new User();
        newUser.setEmail(form.getEmail());
        newUser.setPassword(form.getPassword());
        newUser.setUsername(form.getUsername());
        authMgr.register(newUser);
    }

    @GetMapping(value = "/verify/username/{username}")
    public Result checkUsername(@PathVariable String username) {
        // 校验用户名
        if(!Validator.isUsername(username)){
            return Result.error(ResultCode.ILLEGAL_PARAM,"用户名格式不正确");
        }
        User usernameUser = userService.findByUsername(username);
        if(usernameUser != null){
            return Result.error(ResultCode.ILLEGAL_PARAM,"该用户名已存在");
        }
        return Result.success();
    }

    @GetMapping(value = "/verify/email/{email:.+}")
    public Result verifyEmail(@PathVariable String email) {
        // 校验邮箱
        if(!Validator.isEmail(email)){
            return Result.error(ResultCode.ILLEGAL_PARAM,"邮箱格式不正确");
        }
        User usernameUser = userService.findByEmail(email);
        if(usernameUser != null){
            return Result.error(ResultCode.ILLEGAL_PARAM,"该邮箱已存在");
        }
        return Result.success();
    }

    @ApiOperation(value = "激活")
    @GetMapping(value = "/active/{token:.+}")
    public void active(@PathVariable("token") String token) {
        authMgr.active(token);
    }


    @ApiOperation(value = "发送激活邮件")
    @GetMapping(value = "/send/active/{username}")
    public Result sendActiveMail(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if(user == null) {
            return Result.error(ResultCode.ILLEGAL_PARAM,"账号不存在");
        }
        if(user.getIsActive()) {
            return Result.error(ResultCode.ILLEGAL_OP,"账号已激活过");
        }
        authMgr.sendActiveMail(user, user.getUserCode());
        return Result.success();
    }

    @ApiOperation(value = "发送忘记密码邮件")
    @GetMapping(value = "/forget/password/{email:.+}")
    public Result checkEmail(@PathVariable("email") String email) {
        if(!Validator.isEmail(email)){
            return Result.error(ResultCode.ILLEGAL_PARAM,"邮箱格式不正确");
        }
//        User usernameUser = userService.findByEmail(email);
//        if(usernameUser == null){
//            return Result.error(ResultCode.ILLEGAL_PARAM,"邮箱不存在");
//        }
        User user = userService.findByEmail(email);
        if(user == null) {
            return Result.error(ResultCode.ILLEGAL_PARAM,"邮箱不存在");
        }
        authMgr.sendForgetPasswordEmail(user);
        return Result.success();
    }

    @ApiOperation(value = "密码重置")
    @PostMapping(value = "/reset/password/{token:.+}")
    public Result resetPwd(@PathVariable("token") String token, @RequestBody @Valid ResetPasswordForm form) throws AccountNotFoundException {
        String userCode = authMgr.checkForgetPwdCode(token);
        User user = userService.findByUserCode(userCode);
        if(user == null) {
            return Result.error(ResultCode.ILLEGAL_OP,"账号不存在");
        }
        userService.resetPwd(userCode, form.getNewpassword());
        return Result.success();
    }


    @RequestMapping(value = "/captcha", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getCaptcha(@ApiIgnore HttpServletResponse response){
        //生成验证码
        String uuid = Identities.uuid();
        Captcha captcha = new Captcha.Builder(CAPTCHA_W, CAPTCHA_H)
                .addText().addBackground(new GradiatedBackgroundProducer())
                .gimp(new FishEyeGimpyRenderer())
                .build();

        //将验证码以<key,value>形式缓存到redis
        redisLoginMgr.loginCaptcha(uuid, captcha.getAnswer());
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

    @PostMapping(value = "/logout")
    public void logout(){
        redisLoginMgr.logout(currentUser.getUsername());
    }

}
