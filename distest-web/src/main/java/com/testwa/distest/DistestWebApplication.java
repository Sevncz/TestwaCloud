package com.testwa.distest;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.testwa.distest.server.mongo.repository.Impl.CommonMongoRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@SpringBootApplication
@EnableMongoRepositories(repositoryBaseClass = CommonMongoRepositoryImpl.class, basePackages = {"com.testwa.distest.server.mongo.repository"})
@EnableScheduling
@EnableCaching
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableRabbit
@EnableEurekaClient
public class DistestWebApplication extends AsyncConfigurerSupport {

	@Bean
	public SpringAnnotationScanner springAnnotationScanner(SocketIOServer ssrv) {
		return new SpringAnnotationScanner(ssrv);
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1000);
		executor.setMaxPoolSize(1000);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("DT-web-");
		executor.initialize();
		return executor;
	}

	public static void main(String[] args) {
		SpringApplication.run(DistestWebApplication.class, args);

		String androidHome = System.getenv("ANDROID_HOME");
		if(StringUtils.isBlank(androidHome)){
			log.warn("ANDROID_HOME not found");
		}else{
			log.info("androidHome: {}", androidHome);
		}
	}

}
