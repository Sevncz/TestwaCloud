package com.testwa.distest.server.quartz.service;

import java.io.IOException;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.testwa.distest.server.quartz.AutowiringSpringBeanJobFactory;
import com.testwa.distest.server.quartz.QuartzJobFactoryBean;
import com.testwa.distest.server.quartz.model.ScheduleJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


@Configuration
public class SchedulerServiceImpl implements SchedulerService{

	private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	private SchedulerFactoryBean schedulerFactoryBean;
    
    @Autowired
	AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
    
    @PostConstruct
    void init() {
        schedulerFactoryBean = quartzScheduler();
    }
    
    @Bean
    public SchedulerFactoryBean quartzScheduler() {

    	try {       	
        	SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
        	quartzScheduler.setOverwriteExistingJobs(true);
    		quartzScheduler.setSchedulerName("wms-quartz-scheduler");
     		quartzScheduler.setJobFactory(autowiringSpringBeanJobFactory);
    		quartzScheduler.setQuartzProperties(quartzProperties());
    		return quartzScheduler;
        } catch ( Exception e ) {
        	logger.error("Quartz Scheduler can not be initialized, the error is " + e.getMessage());
        	return null;
        }   
    }

    @PreDestroy
    void destroy() {
        try {
            schedulerFactoryBean.destroy();
        } catch ( Exception e ) {
        	logger.error("Quartz Scheduler can not be shutdown, the error is " + e.getMessage(), e);
        }
    }

	
	@Bean
	public Properties quartzProperties() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (IOException e) {
			logger.error("Quartz Scheduler can not read properties file, the error is " + e.getMessage());
		}

		return properties;
	}

	@Override
	public void register(JobDetail jobDetail, CronTrigger cronTrigger) {

		try {
            if(schedulerFactoryBean.getScheduler().checkExists(cronTrigger.getKey())){
                // Option one:
                schedulerFactoryBean.getScheduler().rescheduleJob(cronTrigger.getKey(), cronTrigger);

                //Option two:
                //scheduler.getScheduler().resumeTrigger(cronTrigger.getKey());
                //logger.info("Quartz Scheduler resume trigger " + cronTrigger.getKey());

                //Option three:
//				logger.info("Quartz Scheduler keep the trigger status " + cronTrigger.getKey());

            }else{
                schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, cronTrigger);
                logger.info("Quartz Scheduler register new trigger " +  cronTrigger.getKey());
            }
			/*
			 * We have 3 ways to restart the application
			 * 1. Reschedule the job to it's original schedule.
			 * 2. Resume the job to keep the latest schedule. It will also restart the job that been paused.
			 * 3. Do nothing, the jobs will remain the same as they are before the restart.
			 *
			 * Here we choose option 2. The benefit is in the future we can use the reschedule function to manage the schedule in DB.
			 * We can rely on the persistent of the schedule in DB.
			 */

		} catch (SchedulerException e) {
			logger.error("Quartz Scheduler can not register trigger " +  cronTrigger.getKey() + ". The error is " + e.getMessage(), e);
		}
	}

	@Override
	public void reschedule(CronTrigger cronTrigger) {
		try {
			logger.info("Reschedule trigger " + cronTrigger.getKey());
			logger.info("The new schedule is " +cronTrigger.getCronExpression());

            schedulerFactoryBean.getScheduler().rescheduleJob(cronTrigger.getKey(), cronTrigger);
			
		} catch (SchedulerException e) {
			logger.error("Quartz Scheduler can not reschedule trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
		}
		
	}

	@Override
	public void pause(CronTrigger cronTrigger) {
		try{
			logger.info("Pause trigger " + cronTrigger.getKey());
            schedulerFactoryBean.getScheduler().pauseTrigger(cronTrigger.getKey());
			
		} catch (Exception e) {
			logger.error("Quartz Scheduler can not pause trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
		}
		
	}

	@Override
	public void resume(CronTrigger cronTrigger) {
		try{
			logger.info("Pause trigger " + cronTrigger.getKey());
            schedulerFactoryBean.getScheduler().resumeTrigger(cronTrigger.getKey());
			
		} catch (Exception e) {
			logger.error("Quartz Scheduler can not resume trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
		}

	}

	@Override
	public void register(ScheduleJob job) {
		try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
			TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
			//获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
			CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
			if(trigger == null || !scheduler.checkExists(trigger.getKey())){
				JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactoryBean.class)
						.withIdentity(job.getJobName(), job.getJobGroup()).build();
				jobDetail.getJobDataMap().put("scheduleJob", job);

				//表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

				//按新的cronExpression表达式构建一个新的trigger
				trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(scheduleBuilder).build();
                if(scheduler.isShutdown()){
                    scheduler.start();
                }
                scheduler.scheduleJob(jobDetail, trigger);
			} else {
				// Trigger已存在，那么更新相应的定时设置
				//表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

				//按新的cronExpression表达式重新构建trigger
				trigger = trigger.getTriggerBuilder()
						.withIdentity(triggerKey)
						.withSchedule(scheduleBuilder).build();

				//按新的trigger重新设置job执行
				scheduler.rescheduleJob(triggerKey, trigger);
			}

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void remove(ScheduleJob job) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        try {
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startAllJobs(){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            if(scheduler.isShutdown()){
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void shutdownAllJobs(){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            if(!scheduler.isShutdown()){
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List getAllJobs() {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        List<String> jobs = new ArrayList<>();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {

                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();

                    jobs.add(jobName);
                }

            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobs;
    }


    @Override
    public void pauseAll(){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pause(ScheduleJob job){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            Set<JobKey> jk = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(job.getJobGroup()));
            for(JobKey k : jk){
                if(k.getName().equals(job.getJobName())){
                    scheduler.pauseJob(k);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void resumeAll(){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void resume(ScheduleJob job){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            Set<JobKey> jk = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(job.getJobGroup()));
            for(JobKey k : jk){
                if(k.getName().equals(job.getJobName())){
                    scheduler.resumeJob(k);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

}