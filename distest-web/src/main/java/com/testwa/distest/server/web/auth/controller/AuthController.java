package com.testwa.distest.server.web.auth.controller;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountAlreadyExistException;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ParamsFormatException;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.Validator;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.config.security.JwtAuthenticationRequest;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.form.RegisterForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.mgr.AuthMgr;
import com.testwa.distest.server.web.auth.mgr.RedisLoginMgr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Api("登录注册相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/auth")
public class AuthController extends BaseController {

    @Value("${jwt.header}")
    private String tokenHeader;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthMgr authMgr;
    @Autowired
    private RedisLoginMgr redisLoginMgr;

    @ApiOperation(value = "登录")
    @ApiImplicitParam(name = "authenticationRequest", value = "JWT登录验证类", required = true, dataType = "JwtAuthenticationRequest")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Result createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletRequest request) throws AuthorizedException {
        String ip;
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        }else{
            ip = request.getHeader("x-forwarded-for");
        }
        return ok(authMgr.login(authenticationRequest.getUsername(), authenticationRequest.getPassword(), ip));
    }


    @ApiOperation(value = "刷新Token")
    @ApiImplicitParam(name = "request", value = "请求信息（带有tokenHeader）", required = true, dataType = "HttpServletRequest")
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public Result refreshAndGetAuthenticationToken(HttpServletRequest request) throws AuthorizedException {
        String token = request.getHeader(tokenHeader);
        JwtAuthenticationResponse response = authMgr.refresh(token);
        return ok(response);
    }

    @ApiOperation(value = "注册")
    @ApiImplicitParam(name = "form", value = "注册form", required = true, dataType = "RegisterForm")
    @RequestMapping(value = "/register", method= RequestMethod.POST, produces={"application/json"})
    public Result register(@Valid @RequestBody final RegisterForm form)
            throws ServletException, ParamsFormatException, AccountException, AccountAlreadyExistException {

        // 校验用户名
        if(!Validator.isUsername(form.username)){
            throw new ParamsFormatException("用户名格式不正确");
        }

        // 校验邮箱
        if(!Validator.isEmail(form.email)){
            throw new ParamsFormatException("邮箱格式不正确");
        }
        userService.save(form);
        return ok();
    }

    private static int captchaW = 200;
    private static int captchaH = 60;

    @RequestMapping(value = "/captcha", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getCaptcha(@ApiIgnore HttpServletResponse response){
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


    @RequestMapping(value = "/logout", method= RequestMethod.POST, produces={"application/json"})
    public Result logout(HttpServletRequest request,
                         HttpServletResponse response){
        redisLoginMgr.logout(getCurrentUsername());
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

    @RequestMapping(value = "/verify/email/{email}", method= RequestMethod.GET)
    public Result checkEmail(@PathVariable String email) throws ParamsFormatException, AccountAlreadyExistException {
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

}
