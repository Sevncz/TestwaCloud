package com.testwa.distest.common.validator;

import com.testwa.core.base.exception.ParamsFormatException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

@Aspect
@Configuration
public class RequestParamValidAspect {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final ExecutableValidator methodValidator = factory.getValidator().forExecutables();
    private final Validator beanValidator = factory.getValidator();

    private <T> Set<ConstraintViolation<T>> validMethodParams(T obj, Method method, Object [] params){
        return methodValidator.validateParameters(obj, method, params);
    }

    private <T> Set<ConstraintViolation<T>> validBeanParams(T bean) {
        return beanValidator.validate(bean);
    }

    @Pointcut("execution(* com.testwa.distest.server.*.*(..))")
    public void soaServiceBefore(){}

    /* * 通过连接点切入 */
    @Before("soaServiceBefore()")
    public void twiceAsOld1(JoinPoint point) throws ParamsFormatException {
        //  获得切入目标对象
        Object target = point.getThis();
        // 获得切入方法参数
        Object [] args = point.getArgs();
        // 获得切入的方法
        Method method = ((MethodSignature)point.getSignature()).getMethod();

        // 校验以基本数据类型 为方法参数的
        Set<ConstraintViolation<Object>> validResult = validMethodParams(target, method, args);

        Iterator<ConstraintViolation<Object>> violationIterator = validResult.iterator();
        while (violationIterator.hasNext()) {
            // 此处可以抛个异常提示用户参数输入格式不正确
            System.out.println("method check---------" + violationIterator.next().getMessage());
            throw new ParamsFormatException("请求参数格式错误");
        }

        // 校验以java bean对象 为方法参数的 
        for (Object bean : args) {
            if (null != bean) {
                validResult = validBeanParams(bean);
                violationIterator = validResult.iterator();
                while (violationIterator.hasNext()) {
                    // 此处可以抛个异常提示用户参数输入格式不正确
                    System.out.println("bean check-------" + violationIterator.next().getMessage());
                    throw new ParamsFormatException("请求参数格式错误");
                }
            }
        }
    }
}