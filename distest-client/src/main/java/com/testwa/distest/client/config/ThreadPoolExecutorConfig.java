package com.testwa.distest.client.config;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.core.task.TaskExecutor;import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-18 16:33 **/@Configurationpublic class ThreadPoolExecutorConfig {    @Bean    public TaskExecutor getAsyncExecutor() {        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();        executor.setCorePoolSize(10);        executor.setMaxPoolSize(50);        executor.setQueueCapacity(500);        executor.setThreadNamePrefix("TestwaTask-");        executor.initialize();        return executor;    }}