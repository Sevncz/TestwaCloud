package com.testwa.distest.server.security.controller;

import com.testwa.distest.server.model.message.Result;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.security.JwtAuthenticationRequest;
import com.testwa.distest.server.security.JwtTokenUtil;
import com.testwa.distest.server.security.JwtUser;
import com.testwa.distest.server.security.service.JwtAuthenticationResponse;
import com.testwa.distest.server.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

@RestController
public class AuthenticationRestController extends BaseController{

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${jwt.access_token.expiration}")
    private Long access_token_expiration;

    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    public Result createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) throws AuthenticationException {

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

        // Return the token
        return ok(new JwtAuthenticationResponse(access_token, refresh_token, access_token_expiration));
    }

    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public Result refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String access_token = jwtTokenUtil.refreshToken(token);
            return ok(new JwtAuthenticationResponse(access_token, token, access_token_expiration));
        } else {
            return fail(ResultCode.ILLEGAL_TOKEN.getValue(), "");
        }
    }

}
