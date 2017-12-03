package com.testwa.distest.server.web.auth.mgr;

import com.google.common.net.InetAddresses;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.config.security.JwtAuthenticationResponse;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.config.security.JwtUser;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import lombok.extern.log4j.Log4j2;
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

    public JwtAuthenticationResponse login(String username, String password, String ip) throws BadCredentialsException{

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
        return new JwtAuthenticationResponse(access_token, refresh_token, access_token_expiration);
    }

    public JwtAuthenticationResponse refresh(String token) throws AuthorizedException {
        boolean isRefresh = jwtTokenUtil.isRefreshToken(token);
        if(isRefresh){

            String username = jwtTokenUtil.getUsernameFromToken(token);
            JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

            if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
                String access_token = jwtTokenUtil.refreshToken(token);
                return new JwtAuthenticationResponse(access_token, token, access_token_expiration);
            }
        }
        throw new AuthorizedException("非法的token，无法刷新");
    }

}
