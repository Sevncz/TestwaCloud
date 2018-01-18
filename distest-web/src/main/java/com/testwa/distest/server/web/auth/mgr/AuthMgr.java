package com.testwa.distest.server.web.auth.mgr;

import com.google.common.net.InetAddresses;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.LoginInfoNotFoundException;
import com.testwa.core.base.vo.Result;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.config.security.JwtUser;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import lombok.extern.log4j.Log4j2;
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

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Date;

@Log4j2
@Component
public class AuthMgr {

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

    public JwtAuthenticationResponse login(String username, String password, Integer ip) throws BadCredentialsException, LoginInfoNotFoundException {
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
        user.setLoginIp(ip);
        userService.update(user);
        redisLoginMgr.login(username, access_token);
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

}
