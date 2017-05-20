package com.testwa.distest.server.monitor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 纪录耗时请求
 * Created by wen on 2016/11/12.
 */
@Aspect
@Component
class WebRequestTimeLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(WebRequestTimeLogAspect.class);

    ThreadLocal<Long> startTime = new ThreadLocal<Long>();

    @Pointcut("execution(public * com.testwa.*.web..*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        startTime.set(System.currentTimeMillis());
    }

    @AfterReturning("webLog()")
    public void  doAfterReturning(JoinPoint joinPoint){
        // 处理完请求，返回内容
        Long statTime = startTime.get();
        Long endTime = System.currentTimeMillis();
        if(endTime - statTime > 100){
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes != null){
                HttpServletRequest request = attributes.getRequest();
                logger.info("TIME : {} ", endTime - statTime);
                logger.info("URL : {}", request.getRequestURL().toString());
                logger.info("HTTP_METHOD : {}", request.getMethod());
                logger.info("IP : {}", request.getRemoteAddr());
                logger.info("CLASS_METHOD : {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
                logger.info("ARGS : {}", Arrays.toString(joinPoint.getArgs()));
            }
        }
    }
}
