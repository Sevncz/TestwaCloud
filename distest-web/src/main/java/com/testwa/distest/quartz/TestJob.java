package com.testwa.distest.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class TestJob implements BaseJob {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("test job----PreviousFireTime={},NextFireTime={},FireTime={}" ,context.getPreviousFireTime(),context.getNextFireTime(),context.getFireTime());
    }
}