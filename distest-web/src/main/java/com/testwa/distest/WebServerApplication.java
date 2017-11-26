package com.testwa.distest;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.mongodb.MongoClientURI;
import com.testwa.distest.server.mvc.repository.Impl.CommonMongoRepositoryImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.UnknownHostException;
import java.util.concurrent.Executor;

@EnableDiscoveryClient
@SpringBootApplication
@EnableMongoRepositories(repositoryBaseClass = CommonMongoRepositoryImpl.class, basePackages = {"com.testwa.distest.server.mvc.repository"})
@EnableScheduling
@EnableCaching
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebServerApplication extends AsyncConfigurerSupport {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

	@Bean
	public SpringAnnotationScanner springAnnotationScanner(SocketIOServer ssrv) {
		return new SpringAnnotationScanner(ssrv);
	}

	@Bean
	public MongoDbFactory mongoDbFactory(){
		MongoClientURI uri = new MongoClientURI(mongoUri);
		try {
			return new SimpleMongoDbFactory(uri);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Bean
	public MappingMongoConverter mongoConverter() throws Exception {
		MongoMappingContext mappingContext = new MongoMappingContext();
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory());
		MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
//		mongoConverter.setCustomConversions(customConversions());
		return mongoConverter;
	}

	@Bean(autowire = Autowire.BY_NAME, name = "mongoTemplate")
	public MongoTemplate customMongoTemplate() {
		try {
			return new MongoTemplate(mongoDbFactory(), mongoConverter());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1000);
		executor.setMaxPoolSize(1000);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("ServerLookup-");
		executor.initialize();
		return executor;
	}

	public static void main(String[] args) {
		SpringApplication.run(WebServerApplication.class, args);
	}

}
