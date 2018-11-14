package com.testwa.distest.server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testwa.distest.exception.ExceptionAdvice;
import com.testwa.distest.server.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RestControllerAspect {
    private ObjectMapper mapper;

    // 统一注入的用户对象，后面解释
    private User user;

    public RestControllerAspect(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public RestControllerAspect setUser(User user) {
        this.user = user;
        return this;
    }


    //RestController 注解标注的类和方法
    @Around("@within(org.springframework.web.bind.annotation.RestController) || @annotation(org.springframework.web.bind.annotation.RestController)")
    public Object apiLog(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        if (!needToLog(method)) {
            return point.proceed();
        }

        String name = user.getUsername();
        String methodName = getMethodName(point);
        String params = getParamsJson(point);

        log.info("Started request -> requester [{}] method [{}] params [{}]", name, methodName, params);
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        log.info("End request -> requester[{}] method [{}] params [{}] and response is [{}] cost [{}] millis", name, methodName, params, result, System.currentTimeMillis() - start);
        return result;
    }

    // 获取请求数据
    private String getParamsJson(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            String str;
            if (arg instanceof HttpServletResponse) {
                str = HttpServletResponse.class.getSimpleName();
            } else if (arg instanceof HttpServletRequest) {
                str = HttpServletRequest.class.getSimpleName();
            } else if (arg instanceof MultipartFile) {
                long size = ((MultipartFile) arg).getSize();
                str = MultipartFile.class.getSimpleName() + "size:" + size;
            } else {
                try {
                    str = mapper.writeValueAsString(arg);
                } catch (JsonProcessingException e) {
                    // 这里基本不可能进入
                    log.error("json process error", e);
                    str = "error object";
                }
            }
            sb.append(str).append(",");
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        return "";
    }

    // 获取方法名
    private String getMethodName(ProceedingJoinPoint point) {
        String methodName = point.getSignature().toShortString();
        String SHORT_METHOD_NAME_SUFFIX = "(..)";
        if (methodName.endsWith(SHORT_METHOD_NAME_SUFFIX)) {
            methodName = methodName.substring(0, methodName.length() - SHORT_METHOD_NAME_SUFFIX.length());
        }
        return methodName;
    }

    /**
     * 排除获取数据的方法，因为不会更改任何信息。
     * 另外还可以通过注解标记是否需要日志
     * <br/>
     * 排除全局异常处理类
     *
     * @param method 方法
     * @return true/false
     */
    private boolean needToLog(Method method) {
        return !method.getName().startsWith("get") && !method.getDeclaringClass().equals(ExceptionAdvice.class);
    }

}