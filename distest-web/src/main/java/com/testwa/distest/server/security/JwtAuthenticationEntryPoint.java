package com.testwa.distest.server.security;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.server.exception.AuthorizedException;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        Result<String> r = new Result<>();
        r.setCode(ResultCode.ILLEGAL_TOKEN.getValue());
        r.setMessage("非法的token");
        response.getWriter().println(JSON.toJSON(r));
        response.getWriter().flush();

    }
}