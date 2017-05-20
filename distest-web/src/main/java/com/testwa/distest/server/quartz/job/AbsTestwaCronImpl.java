package com.testwa.distest.server.quartz.job;

import java.text.ParseException;

import com.testwa.distest.server.quartz.service.SchedulerService;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;


public abstract class AbsTestwaCronImpl implements TestwaCron {

	private static final Logger logger = LoggerFactory.getLogger(AbsTestwaCronImpl.class);

	@Autowired
	SchedulerService schedulerService;

	protected CronTriggerFactoryBean cronTriggerFactoryBean;
	protected JobDetailFactoryBean jobDetailFactoryBean;


	public void register(){
		schedulerService.register(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
		logger.info("Regirster trigger " +  cronTriggerFactoryBean.getObject().getKey() + " with schedule " + cronTriggerFactoryBean.getObject().getCronExpression());

	}

	public void reschedule( String cronExpression ) {
		try {
			CronTriggerImpl cronTriggerImpl = (CronTriggerImpl)(cronTriggerFactoryBean.getObject());
			cronTriggerImpl.setCronExpression(cronExpression);
			schedulerService.reschedule(cronTriggerImpl);

			logger.info("Reschedule trigger " +  cronTriggerImpl.getKey() + " with new schedule " + cronTriggerImpl.getCronExpression());

		} catch (ParseException e) {
			logger.error("Quartz Scheduler can not reschedule with the expression " + cronExpression + ". the error is " + e.getMessage(), e);

		}
	}

	public void pause() {

		try {
			CronTriggerImpl cronTriggerImpl = (CronTriggerImpl)(cronTriggerFactoryBean.getObject());
			schedulerService.pause(cronTriggerImpl);

			logger.info("Pause trigger " +  cronTriggerImpl.getKey() + " with new schedule " + cronTriggerImpl.getCronExpression());

		} catch (Exception e) {
			logger.error("Quartz Scheduler can not Pause the trigger, the error is " + e.getMessage(), e);

		}
	}

	public void resume() {

		try {
			CronTriggerImpl cronTriggerImpl = (CronTriggerImpl)(cronTriggerFactoryBean.getObject());
			schedulerService.resume(cronTriggerImpl);

			logger.info("Pause trigger " +  cronTriggerImpl.getKey() + " with new schedule " + cronTriggerImpl.getCronExpression());

		} catch (Exception e) {
			logger.error("Quartz Scheduler can not Pause the trigger, the error is " + e.getMessage(), e);

		}
	}

}