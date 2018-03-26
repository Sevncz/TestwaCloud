package com.testwa.distest.client.component.executor;import lombok.extern.slf4j.Slf4j;import net.sf.cglib.proxy.MethodInterceptor;import net.sf.cglib.proxy.MethodProxy;import java.lang.reflect.Method;import java.util.Arrays;@Slf4jpublic class CglibProxy implements MethodInterceptor {    @Override    public Object intercept(Object proxy, Method method, Object[] args,                            MethodProxy invocation) throws Throwable {        // 日志开始        System.out.println("the method [" + method.getName() + "]"                + "begin with args (" + Arrays.toString(args) + ")");        Object result = invocation.invokeSuper(proxy, args);        // 日志结束        System.out.println("the method [" + method.getName() + "]"                + "end with result (" + result + ")");         return result;    }}