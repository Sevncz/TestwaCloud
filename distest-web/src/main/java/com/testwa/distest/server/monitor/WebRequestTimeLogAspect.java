package com.testwa.distest.server.monitor;

import com.testwa.distest.common.context.ThreadContext;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@Aspect
@Component
class WebRequestTimeLogAspect {

    @Pointcut("execution(public * com.testwa.*.server.web..*Controller.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        ThreadContext.init();
        ThreadContext.putRequestBeforeTime(System.currentTimeMillis());
        log.info("befor: {}， {}", ThreadContext.getRequestBeforeTime(), joinPoint.getTarget());
    }

    @AfterReturning("webLog()")
    public void  doAfterReturning(JoinPoint joinPoint){
        // 处理完请求，返回内容
        Long startTime = ThreadContext.getRequestBeforeTime();
        Long endTime = System.currentTimeMillis();
        log.info("after: {}， {}", endTime, joinPoint.getTarget());
        if(endTime - startTime > 300){
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes != null){
                HttpServletRequest request = attributes.getRequest();
                log.info("TIME : {} ", endTime - startTime);
                log.info("URL : {}", request.getRequestURL().toString());
                log.info("HTTP_METHOD : {}", request.getMethod());
                log.info("IP : {}", request.getRemoteAddr());
                log.info("CLASS_METHOD : {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
                log.info("ARGS : {}", Arrays.toString(joinPoint.getArgs()));
            }
        }
    }
}
