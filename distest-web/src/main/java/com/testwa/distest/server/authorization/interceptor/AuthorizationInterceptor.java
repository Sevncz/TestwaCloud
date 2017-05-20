package com.testwa.distest.server.authorization.interceptor;

import com.testwa.distest.server.authorization.Constants;
import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.service.security.TestwaTokenService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Autowired
    private TestwaTokenService tokenService;

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        String token = tokenService.getToken(request);
        if(StringUtils.isNotBlank(token)){
            try {
                Claims c = tokenService.parserToken(token);
                //如果token验证成功，将token对应的用户id存在request中，便于之后注入
                request.setAttribute(Constants.CURRENT_USER_ID, c.getId());
                return true;
            } catch (Exception e) {
                log.error("Invalid token. Get token value from cookie, {}", token);
            }
        }else{
            log.error("Token was null");
        }
        // 暂时去掉token校验
        if (method.getAnnotation(Authorization.class) != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return false;
    }
}