package com.testwa.distest.server.quartz.service;

import com.testwa.distest.server.quartz.model.ScheduleJob;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;

import java.util.List;

public interface SchedulerService {

	void register(JobDetail jobDetail, CronTrigger cronTrigger);

	void reschedule(CronTrigger cronTrigger);

	void pause(CronTrigger cronTrigger);
	
	void resume(CronTrigger cronTrigger);

	void register(ScheduleJob job);

	void remove(ScheduleJob job);

	void startAllJobs();

	void shutdownAllJobs();

    List getAllJobs();

    void pauseAll();

    void pause(ScheduleJob job);

    void resumeAll();

    void resume(ScheduleJob job);
}