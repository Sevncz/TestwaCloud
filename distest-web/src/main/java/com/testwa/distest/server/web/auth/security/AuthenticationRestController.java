package com.testwa.distest.server.web.auth.security;

import com.google.common.net.InetAddresses;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.AccountAlreadyExistException;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.ResultCode;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.jwt.JwtAuthenticationRequest;
import com.testwa.distest.server.web.auth.jwt.JwtTokenUtil;
import com.testwa.distest.server.web.auth.dto.JwtUser;
import com.testwa.distest.server.web.auth.service.JwtAuthenticationResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Date;

@Log4j2
@RestController
public class AuthenticationRestController extends BaseController {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;

    @Value("${jwt.access_token.expiration}")
    private Long access_token_expiration;

    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    public Result createAuthenticationToken(HttpServletRequest httpServletRequest,
                                            @RequestBody JwtAuthenticationRequest authenticationRequest,
                                            Device device) throws AuthenticationException, AccountException, AccountAlreadyExistException {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String access_token = jwtTokenUtil.generateAccessToken(userDetails, device);
        final String refresh_token = jwtTokenUtil.generateRefreshToken(userDetails);

        User user = userService.findByUsername(authentication.getName());
        if(user.getLoginTime() != null){
            user.setLastLoginTime(user.getLoginTime());
        }
        user.setLoginTime(new Date());
        if(user.getLoginIp() != null){
            user.setLastLoginIp(user.getLoginIp());
        }
        String ip;
        if (httpServletRequest.getHeader("x-forwarded-for") == null) {
            ip = httpServletRequest.getRemoteAddr();
        }else{
            ip = httpServletRequest.getHeader("x-forwarded-for");
        }
        InetAddress addr = InetAddresses.forString(ip);
        user.setLoginIp(InetAddresses.coerceToInteger(addr));
        userService.update(user);
        return ok(new JwtAuthenticationResponse(access_token, refresh_token, access_token_expiration));
    }

    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public Result refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        boolean isRefresh = jwtTokenUtil.isRefreshToken(token);
        if(isRefresh){

            String username = jwtTokenUtil.getUsernameFromToken(token);
            JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

            if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
                String access_token = jwtTokenUtil.refreshToken(token);
                return ok(new JwtAuthenticationResponse(access_token, token, access_token_expiration));
            }
        }
        return fail(ResultCode.ILLEGAL_TOKEN, "非法的token，无法刷新");
    }

}
