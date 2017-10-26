package com.testwa.distest.server.web.quartz.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.server.quartz.model.ScheduleJob;
import com.testwa.distest.server.quartz.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/cron")
public class CronController extends BaseController {

	@Autowired
	private SchedulerService schedulerService;


	@RequestMapping(value = "register", method= RequestMethod.GET)
	public void register() {
		//这里获取任务信息数据
		List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();

		for (int i = 0; i < 3; i++) {
			ScheduleJob job = new ScheduleJob();
			job.setJobId("10001" + i);
			job.setJobName("JobName_" + i);
			job.setJobGroup("dataWork");
			job.setJobStatus("1");
			job.setCronExpression("0/5 * * * * ?");
			job.setDesc("数据导入任务");
			job.setTargetObject("anotherService");
			job.setTargetMethod("test");

			Object[] params = new Object[1];
			params[0] = job.getJobName();
			job.setTargetParamsValue(params);

//			Class[] classes = new Class[1];
//			classes[0] = String.class;
//			job.setTargetParamsClasses(classes);

			jobList.add(job);
		}

		for (ScheduleJob job : jobList) {

			schedulerService.register(job);


		}
	}

	@RequestMapping(value = "alljobs", method= RequestMethod.GET)
	public Result allJobs() {

		List result = schedulerService.getAllJobs();

		return ok(result);
	}

	@RequestMapping(value = "pause/all", method= RequestMethod.GET)
	public Result shutdownAll() {

		schedulerService.pauseAll();

		return ok();
	}


	@RequestMapping(value = "resume/all", method= RequestMethod.GET)
	public Result startAll() {

		schedulerService.resumeAll();

		return ok();
	}

}