package com.testwa.distest.client;

import com.testwa.distest.client.control.client.grpc.pool.GClientPool;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext; // Spring应用上下文环境

    public static GClientPool getGClientBean() {
        return (GClientPool) applicationContext.getBean("gClientPool");
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanDetail(String beanName) throws BeansException {
        return (T) applicationContext.getBean(beanName);
    }



}