package com.testwa.distest.server.web.auth.mgr;

import com.alibaba.fastjson.JSON;
import com.google.common.net.InetAddresses;
import com.testwa.core.base.exception.*;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.config.security.JwtUser;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.mq.MQService;
import com.testwa.distest.server.service.user.service.AgentLoginLoggerService;
import com.testwa.distest.server.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Date;

@Slf4j
@Component
public class AuthMgr {
    private static final String AGENT_HEADER = "Distest-agent";

    @Value("${jwt.access_token.expiration}")
    private Long access_token_expiration;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisLoginMgr redisLoginMgr;
    @Autowired
    private AgentLoginLoggerService agentLoginLoggerService;
    @Autowired
    private MQService mqService;

    public JwtAuthenticationResponse login(String username, String password, String ip, String userAgent) throws BadCredentialsException, LoginInfoNotFoundException {
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            throw new LoginInfoNotFoundException("登录信息不能为空");
        }

        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
        final Authentication authentication = authenticationManager.authenticate(upToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String access_token = jwtTokenUtil.generateAccessToken(userDetails);
        final String refresh_token = jwtTokenUtil.generateRefreshToken(userDetails);

        User user = userService.findByUsername(authentication.getName());
        if(user.getLoginTime() != null){
            user.setLastLoginTime(user.getLoginTime());
        }
        user.setLoginTime(new Date());
        if(user.getLoginIp() != null){
            user.setLastLoginIp(user.getLoginIp());
        }
        InetAddress addr = InetAddresses.forString(ip);
        user.setLoginIp(InetAddresses.coerceToInteger(addr));
        userService.update(user);

        log.info("userAgent: {}", userAgent);
        // 判断该请求来自哪
        if(StringUtils.isNotEmpty(userAgent)){
            // 来自客户端
            if(userAgent.indexOf(AGENT_HEADER) == 0){
                // 格式
                // Distest-agent/1.0.0/{"host":"192.168.3.4","javaVersion":"1.8.0_73","mac":"AC-BC-32-A8-F6-A9","osArch":"x86_64","osName":"Mac OS X","osVersion":"10.12.6"}
                String[] agents = userAgent.split("/");

                String agentInfo = agents[2];
                AgentLoginLogger lal = JSON.parseObject(agentInfo, AgentLoginLogger.class);
                lal.setUsername(username);
                lal.setClientVersion(agents[1]);
                lal.setLoginTime(new Date());
                lal.setIp(InetAddresses.coerceToInteger(addr));
                agentLoginLoggerService.save(lal);
                // 替换之前登录的token
                redisLoginMgr.login(username, access_token);
            }

        }

        return new JwtAuthenticationResponse(access_token, refresh_token, access_token_expiration);
    }

    public JwtAuthenticationResponse refresh(String token) throws LoginInfoNotFoundException {
        boolean isRefresh = jwtTokenUtil.isRefreshToken(token);
        if(isRefresh){

            String username = jwtTokenUtil.getUsernameFromToken(token);
            JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

            if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
                String access_token = jwtTokenUtil.refreshToken(token);
                return new JwtAuthenticationResponse(access_token, token, access_token_expiration);
            }
        }
        throw new LoginInfoNotFoundException("Refresh Token 错误");
    }


    public String register(User user) throws AccountAlreadyExistException, AccountException {
        String userCode = userService.save(user);
        sendActiveMail(user, userCode);
        return userCode;
    }

    public void sendActiveMail(User user, String userCode) {
        // 生成激活code
        String token = TokenGenerator.generator(userCode, "register", 48*60*60L);
        // 发邮件
        mqService.sendActiveEmail(user, token);
    }


    /**
     *@Description: 用户账号激活
     *@Param: [token]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/1
     */
    public void active(String token) throws ObjectNotExistsException,  AccountActiveCodeHavaExpiredException{
        String userCode = TokenGenerator.getUserCode(token);
        if(StringUtils.isBlank(userCode)) {
            throw new AccountActiveCodeHavaExpiredException("激活链接已过期");
        }
        User user = userService.findByUserCode(userCode);
        if (user == null) {
            log.info("{} 激活失败，用户不存在", userCode);
            throw new ObjectNotExistsException("激活失败，用户不存在");
        }else if(user.getIsActive()) {
            log.info("{} 激活失败，用户已激活", userCode);
            throw new ObjectNotExistsException("激活失败，您已激活");
        }else{
            userService.updateActiveToTrue(userCode);
        }

    }

    public String checkForgetPwdCode(String token) {
        String userCode = TokenGenerator.getUserCode(token);
        if(StringUtils.isBlank(userCode)){
            throw new AccountActiveCodeHavaExpiredException("非法的路径");
        }
        return userCode;
    }

    public void sendForgetPasswordEmail(User user) {
        // 生成token
        String token = TokenGenerator.generator(user.getUserCode(), "register", 48*60*60L);
        // 发邮件
        mqService.sendForgetPwdEmail(user, token);
    }
}
