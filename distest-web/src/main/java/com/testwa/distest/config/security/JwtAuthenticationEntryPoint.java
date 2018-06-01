package com.testwa.distest.config.security;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.service.UserService;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;
    @Value("${jwt.header}")
    private String tokenHeader;
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String token = request.getHeader(tokenHeader);
        Result<String> r = new Result<>();
        if(StringUtils.isNotEmpty(token)){

            String username = jwtTokenUtil.getUsernameFromToken(token);
            if(StringUtils.isBlank(username)) {
                r.setCode(ResultCode.EXPRIED_TOKEN.getValue());
                r.setMessage("非法的TOKEN");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(JSON.toJSON(r));
                response.getWriter().flush();
                return;
            }

            User user = userService.findByUsername(username);
            if(!user.getIsActive()) {
                r.setCode(ResultCode.ACCOUNT_NOT_ACTIVED.getValue());
                r.setMessage("账号未激活");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(JSON.toJSON(r));
                response.getWriter().flush();
                return;
            }

        }
        r.setCode(ResultCode.NO_LOGIN.getValue());
        r.setMessage("TOKEN为空");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().println(JSON.toJSON(r));
        response.getWriter().flush();
    }
}
