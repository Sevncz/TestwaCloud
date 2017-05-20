package com.testwa.distest.server.config;

import com.testwa.distest.server.authorization.interceptor.AuthorizationInterceptor;
import com.testwa.distest.server.authorization.resolvers.CurrentUserMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * 配置类，增加自定义拦截器和解析器
 * @see com.testwa.distest.server.authorization.resolvers.CurrentUserMethodArgumentResolver
 * @see com.testwa.distest.server.authorization.interceptor.AuthorizationInterceptor
 * @author Wen
 * @date 2015/7/30.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    private CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // "/project/*", "/app/*", "/device/*", "/script/*", "/case/*", "/report/*"
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/project/**")
                .addPathPatterns("/app/**")
                .addPathPatterns("/device/**")
                .addPathPatterns("/script/**")
                .addPathPatterns("/case/**")
                .addPathPatterns("/report/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(currentUserMethodArgumentResolver);
    }
}