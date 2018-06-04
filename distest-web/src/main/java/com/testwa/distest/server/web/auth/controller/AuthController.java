package com.testwa.distest.server.web.auth.controller;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.Validator;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.config.security.JwtAuthenticationRequest;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
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
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("登录注册相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/auth")
public class AuthController extends BaseController {
    private static final int captchaW = 200;
    private static final int captchaH = 60;

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

    @ApiOperation(value = "登录")
    @ApiImplicitParam(name = "authenticationRequest", value = "JWT登录验证类", required = true, dataType = "JwtAuthenticationRequest")
    @PostMapping(value = "login")
    public Result createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletRequest request) throws AuthorizedException, LoginInfoNotFoundException, AccountNoActiveException {
        String ip;
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        }else{
            ip = request.getHeader("x-forwarded-for");
        }
        String userAgent = request.getHeader("user-agent");
        userValidator.validateActive(authenticationRequest.getUsername());
        return ok(authMgr.login(authenticationRequest.getUsername(), authenticationRequest.getPassword(), ip, userAgent));
    }


    @ApiOperation(value = "刷新Token")
    @ApiImplicitParam(name = "request", value = "请求信息（带有tokenHeader）", required = true, dataType = "HttpServletRequest")
    @GetMapping(value = "/refresh")
    public Result refreshAndGetAuthenticationToken(HttpServletRequest request) throws LoginInfoNotFoundException {
        String token = request.getHeader(tokenHeader);
        JwtAuthenticationResponse response = authMgr.refresh(token);
        return ok(response);
    }

    @ApiOperation(value = "注册")
    @ApiImplicitParam(name = "form", value = "注册form", required = true, dataType = "RegisterForm")
    @PostMapping(value = "/register")
    public Result register(@Valid @RequestBody final RegisterForm form)
            throws ParamsFormatException, AccountException, ObjectAlreadyExistException {
        // 校验邮箱
        if(StringUtils.isBlank(form.getEmail())) {
            throw new ParamsIsNullException("邮箱不能为空");
        }
        if(!Validator.isEmail(form.getEmail())){
            throw new ParamsFormatException("邮箱格式不正确");
        }
        if(StringUtils.isBlank(form.getPassword())) {
            throw new ParamsIsNullException("密码不能为空");
        }
        if(!Validator.isPassword(form.getPassword())){
            throw new ParamsFormatException("密码格式不正确，数字、字母、特殊字符占2种以，共6到16位");
        }
        if(StringUtils.isBlank(form.getUsername())) {
            throw new ParamsIsNullException("用户名不能为空");
        }

        userValidator.validateEmailHasExist(form.getEmail());
        userValidator.validateUsernameHasExist(form.getUsername());

        User newUser = new User();
        newUser.setEmail(form.getEmail());
        newUser.setPassword(form.getPassword());
        newUser.setUsername(form.getUsername());
        authMgr.register(newUser);
        return ok();
    }

    @RequestMapping(value = "/verify/username/{username}", method= RequestMethod.GET)
    public Result checkUsername(@PathVariable String username) throws AccountAlreadyExistException, ParamsFormatException {
        // 校验用户名
        if(!Validator.isUsername(username)){
            throw new ParamsFormatException("用户名格式不正确");
        }
        User usernameUser = userService.findByUsername(username);
        if(usernameUser != null){
            throw new AccountAlreadyExistException("该用户名已存在");
        }
        return ok();
    }

    @RequestMapping(value = "/verify/email/{email:.+}", method= RequestMethod.GET)
    public Result verifyEmail(@PathVariable String email) throws ParamsFormatException, AccountAlreadyExistException {
        // 校验邮箱
        if(!Validator.isEmail(email)){
            throw new ParamsFormatException("邮箱格式不正确");
        }
        User usernameUser = userService.findByEmail(email);
        if(usernameUser != null){
            throw new AccountAlreadyExistException("该邮箱已存在");
        }
        return ok();
    }

    @ApiOperation(value = "激活")
    @GetMapping(value = "/active/{token:.+}")
    public Result active(@PathVariable("token") String token) throws ObjectNotExistsException,  AccountActiveCodeHavaExpiredException{
        authMgr.active(token);
        return ok();
    }

    @ApiOperation(value = "发送忘记密码邮件")
    @GetMapping(value = "/forget/password/{email:.+}")
    public Result checkEmail(@PathVariable("email") String email) throws AccountNotFoundException {
        if(StringUtils.isBlank(email)) {
            throw new ParamsIsNullException("邮箱不能为空");
        }
        if(!Validator.isEmail(email)) {
            throw new ParamsIsNullException("邮箱格式不正确");
        }
        User user = userService.findByEmail(email);
        if(user == null) {
            throw new AccountNotFoundException("邮箱不存在");
        }
        authMgr.sendForgetPasswordEmail(user);
        return ok();
    }

    @ApiOperation(value = "密码重置")
    @PostMapping(value = "/reset/password/{token:.+}")
    public Result resetPwd(@PathVariable("token") String token, @RequestBody ResetPasswordForm form) throws AccountNotFoundException {
        if(StringUtils.isBlank(token)) {
            throw new ParamsIsNullException("非法的请求链接");
        }
        if(StringUtils.isBlank(form.getNewpassword())) {
            throw new ParamsIsNullException("请输入密码");
        }
        if(!Validator.isPassword(form.getNewpassword())){
            throw new ParamsFormatException("密码格式不正确，数字、字母、特殊字符占2种以上，共6到16位");
        }
        String userCode = authMgr.checkForgetPwdCode(token);
        User user = userService.findByUserCode(userCode);
        if(user == null) {
            throw new AccountNotFoundException("账号不存在");
        }
        userService.resetPwd(userCode, form.getNewpassword());
        return ok();
    }


    @RequestMapping(value = "/captcha", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getCaptcha(@ApiIgnore HttpServletResponse response){
        //生成验证码
        String uuid = Identities.uuid();
        Captcha captcha = new Captcha.Builder(captchaW, captchaH)
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
    public Result logout(HttpServletRequest request,
                         HttpServletResponse response){
        redisLoginMgr.logout(getCurrentUsername());
        return ok();
    }

}
