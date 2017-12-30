package com.testwa.distest.server.quartz.job;

import javax.annotation.PostConstruct;

import com.testwa.distest.server.quartz.service.SimpleService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;


//@Configuration
public class SimpleCron extends AbsTestwaCronImpl{

	private static final Logger logger = LoggerFactory.getLogger(SimpleCron.class);
	
	@PostConstruct
    void init() {
		cronTriggerFactoryBean = simpleCronTriggerFactoryBean();
		jobDetailFactoryBean = simpleJobDetailFactory();
        register();
    }
	
	@Bean
	public JobDetailFactoryBean simpleJobDetailFactory() {
		JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		
		jobDetailFactory.setJobClass(SimpleJob.class);
		jobDetailFactory.setName("SimpleCronJob");
		jobDetailFactory.setGroup("JinJob");
		return jobDetailFactory;
		
	}
	
	@Bean
	public CronTriggerFactoryBean simpleCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		
		cronTriggerFactoryBean.setJobDetail(simpleJobDetailFactory().getObject());
		cronTriggerFactoryBean.setCronExpression("0 0/1 * * * ?");
		cronTriggerFactoryBean.setName("SimpleCronTrigger");
		cronTriggerFactoryBean.setGroup("JinJob");
		return cronTriggerFactoryBean;
	}
	
	public static class SimpleJob implements Job {
		
		@Autowired
		public SimpleService simpleService;
		
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			try{
				logger.info("Running the Job " );
				simpleService.test();
			}catch(Exception e){
				logger.error("Error when running the Job, the error is " + e.getMessage(), e);
			}		
		}
	}

}