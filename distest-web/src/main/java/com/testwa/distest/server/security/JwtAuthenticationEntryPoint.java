package com.testwa.distest.server.security;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.server.exception.AuthorizedException;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Value("${jwt.header}")
    private String tokenHeader;
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String token = request.getHeader(tokenHeader);
        Result<String> r = new Result<>();
        if(StringUtils.isNotEmpty(token)){
            try {
                Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token)
                        .getBody();
            } catch (ExpiredJwtException e) {

                r.setCode(ResultCode.EXPRIED_TOKEN.getValue());
                r.setMessage("token已过期");
                response.getWriter().println(JSON.toJSON(r));
                response.getWriter().flush();
                return;
            }
        }
        r.setCode(ResultCode.ILLEGAL_TOKEN.getValue());
        r.setMessage("非法的token");
        response.getWriter().println(JSON.toJSON(r));
        response.getWriter().flush();

    }
}