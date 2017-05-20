package com.testwa.distest.server.quartz;

import com.testwa.distest.server.quartz.model.ScheduleJob;
import org.quartz.*;
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态运行方法
 */
@Component
//@PersistJobDataAfterExecution
@DisallowConcurrentExecution //确保多个任务不会同时运行
public class QuartzJobFactoryBean extends QuartzJobBean {
    private static final Logger logger = LoggerFactory.getLogger(QuartzJobFactoryBean.class);


    private ScheduleJob scheduleJob;

    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        try {
            Object targetObject = ApplicationContextUtil.getBean(scheduleJob.getTargetObject());
            Method m;
            try {
                Class[] classes = new Class[scheduleJob.getTargetParamsValue().length];
                for(int i=0;i<scheduleJob.getTargetParamsValue().length;i++){
                    Object o = scheduleJob.getTargetParamsValue()[i];
                    classes[i] = o.getClass();
                }
                m = targetObject.getClass().getMethod(scheduleJob.getTargetMethod(), classes);
                m.invoke(targetObject, scheduleJob.getTargetParamsValue());
            } catch (SecurityException e) {
                logger.error(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }

    }

    public void setScheduleJob(ScheduleJob scheduleJob) {
        this.scheduleJob = scheduleJob;
    }

}