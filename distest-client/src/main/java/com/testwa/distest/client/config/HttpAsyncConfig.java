package com.testwa.distest.client.config;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testwa.distest.client.model.AgentInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wen on 16/9/11.
 */
@Configuration
public class HttpAsyncConfig {

    @Value("${application.version}")
    private String applicationVersion;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public CloseableHttpAsyncClient clientHttpRequestFactory() throws IOReactorException {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(200);              //设置最多连接数
        connectionManager.setDefaultMaxPerRoute(20);     //设置每个host最多20个连接数
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)         //设置请求响应超时时间
                .setConnectTimeout(3000)        //设置请求连接超时时间
                .build();
        AgentInfo info = new AgentInfo();
        return HttpAsyncClients.custom()
                .setUserAgent(String.format("Distest-agent/%s/%s", applicationVersion, JSON.toJSONString(info)))
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)    //设置请求配置
                .build();
    }
}