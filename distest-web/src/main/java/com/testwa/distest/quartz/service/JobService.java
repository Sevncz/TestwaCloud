package com.testwa.distest.quartz.service;

import com.testwa.core.base.vo.PageResultVO;
import com.testwa.distest.quartz.JobInfoVO;
import com.testwa.distest.quartz.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobService {

    @Autowired
    private Scheduler scheduler;

    /**
     * 分页查询
     *
     * @return
     */
    public PageResultVO<JobInfoVO> list(int page, int size) {
        PageResultVO<JobInfoVO> resultVO = null;
        try {
            List<JobInfoVO> list = new ArrayList<>();
            for (String groupJob : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(groupJob))) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                        String cronExpression = "", createTime = "";

                        if (trigger instanceof CronTrigger) {
                            CronTrigger cronTrigger = (CronTrigger) trigger;
                            cronExpression = cronTrigger.getCronExpression();
                            createTime = cronTrigger.getDescription();
                        }
                        JobInfoVO info = new JobInfoVO();
                        info.setJobName(jobKey.getName());
                        info.setJobGroup(jobKey.getGroup());
                        info.setJobDescription(jobDetail.getDescription());
                        info.setJobStatus(triggerState.name());
                        info.setCronExpression(cronExpression);
                        info.setCreateTime(createTime);
                        info.setPreviousFireTime(trigger.getPreviousFireTime());
                        info.setNextFireTime(trigger.getNextFireTime());
                        list.add(info);
                    }
                }
            }
            resultVO = new PageResultVO<JobInfoVO>(list.stream().skip((page > 1 ? page - 1 : 0) * size).limit(size).collect(Collectors.toList()), list.size());

        } catch (SchedulerException e) {
            log.error("分页查询定时任务失败，page={},size={},e={}", page, size, e);
        }

        return resultVO;
    }

    /**
     * 添加
     *
     * @param jobName
     * @param jobGroup
     * @param cronExpression
     * @param jobDescription
     */
    public void addJob(String jobName, String jobGroup, String cronExpression, String jobDescription) throws BusinessException {
        addJob(jobName, jobGroup, cronExpression, jobDescription, null);
    }

    public void addJob(String jobName, String jobGroup, String cronExpression, String jobDescription, String params) throws BusinessException {
        if (StringUtils.isAnyBlank(jobName, jobGroup, cronExpression, jobDescription)) {
            throw new BusinessException(String.format("参数错误, jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription));
        }
        String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            log.info("添加jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription);

            if (checkExists(jobName, jobGroup)) {
                log.error("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup));
            }

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(createTime).withSchedule(schedBuilder).build();

            Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(jobName);

            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobKey).withDescription(jobDescription).build();
            jobDetail.getJobDataMap().put("params", params);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException | ClassNotFoundException e) {
            log.error("添加job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException("类名不存在或执行表达式错误");
        }
    }

    /**
     * 添加并且立即执行一次
     *
     * @param jobName
     * @param jobGroup
     * @param cronExpression
     * @param jobDescription
     */
    public void addJobAndProceed(String jobName, String jobGroup, String cronExpression, String jobDescription, String params) throws BusinessException {
        if (StringUtils.isAnyBlank(jobName, jobGroup, cronExpression, jobDescription)) {
            throw new BusinessException(String.format("参数错误, jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription));
        }
        String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            log.info("添加jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription);

            if (checkExists(jobName, jobGroup)) {
                log.error("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup));
            }

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

            CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed();
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(createTime).withSchedule(schedBuilder).build();

            Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(jobName);

            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobKey).withDescription(jobDescription).build();
            jobDetail.getJobDataMap().put("params", params);

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException | ClassNotFoundException e) {
            log.error("添加job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException("类名不存在或执行表达式错误");
        }
    }


    /**
     * 立即执行一次
     *
     * @param jobName
     * @param jobGroup
     * @param jobDescription
     */
    public void proceed(String jobName, String jobGroup, String jobDescription, String params) throws BusinessException {
        if (StringUtils.isAnyBlank(jobName, jobGroup, jobDescription)) {
            throw new BusinessException(String.format("参数错误, jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, jobDescription));
        }
        String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            log.info("添加jobName={},jobGroup={},jobDescription={}", jobName, jobGroup, jobDescription);

            if (checkExists(jobName, jobGroup)) {
                log.error("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job已经存在, jobName={},jobGroup={}", jobName, jobGroup));
            }

//            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

//            Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(jobName);
//            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobKey).withDescription(jobDescription).build();
//            jobDetail.getJobDataMap().put("params", params);

            JobDataMap dataMap = new JobDataMap();
            dataMap.put("params", params);

            scheduler.triggerJob(jobKey, dataMap);
        } catch (SchedulerException e) {
            log.error("添加job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException("类名不存在或执行表达式错误");
        }
    }


    /**
     * 修改
     *
     * @param jobName
     * @param jobGroup
     * @param cronExpression
     * @param jobDescription
     */
    public void edit(String jobName, String jobGroup, String cronExpression, String jobDescription) throws BusinessException {
        if (StringUtils.isAnyBlank(jobName, jobGroup, cronExpression, jobDescription)) {
            throw new BusinessException(String.format("参数错误, jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription));
        }
        String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            log.info("修改jobName={},jobGroup={},cronExpression={},jobDescription={}", jobName, jobGroup, cronExpression, jobDescription);
            if (!checkExists(jobName, jobGroup)) {
                log.error("Job不存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job不存在, jobName={},jobGroup={}", jobName, jobGroup));
            }
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            JobKey jobKey = new JobKey(jobName, jobGroup);
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withDescription(createTime).withSchedule(cronScheduleBuilder).build();

            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDetail.getJobBuilder().withDescription(jobDescription);
            HashSet<Trigger> triggerSet = new HashSet<>();
            triggerSet.add(cronTrigger);

            scheduler.scheduleJob(jobDetail, triggerSet, true);
        } catch (SchedulerException e) {
            log.error("修改job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException("类名不存在或执行表达式错误");
        }
    }

    /**
     * 删除
     *
     * @param jobName
     * @param jobGroup
     */
    public void unschedule(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("删除jobName={},jobGroup={}", jobName, jobGroup);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            if (checkExists(jobName, jobGroup)) {
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
            }
        } catch (SchedulerException e) {
            log.error("删除job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }
    }


    /**
     * 删除
     *
     * @param jobName
     * @param jobGroup
     */
    public void delete(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("删除jobName={},jobGroup={}", jobName, jobGroup);
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("删除job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 暂停
     *
     * @param jobName
     * @param jobGroup
     */
    public void pause(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("暂停jobName={},jobGroup={}", jobName, jobGroup);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            if (!checkExists(jobName, jobGroup)) {
                log.error("Job不存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job不存在, jobName={},jobGroup={}", jobName, jobGroup));
            }
            scheduler.pauseTrigger(triggerKey);
        } catch (SchedulerException e) {
            log.error("暂停job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 终止任务
     *
     * @param jobName
     * @param jobGroup
     */
    public void interrupt(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("终止jobName={},jobGroup={}", jobName, jobGroup);
            if (!checkExists(jobName, jobGroup)) {
                log.error("Job不存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job不存在, jobName={},jobGroup={}", jobName, jobGroup));
            }
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.interrupt(jobKey);
        } catch (SchedulerException e) {
            log.error("暂停job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }

    }

    /**
     * 重启
     *
     * @param jobName
     * @param jobGroup
     */
    public void resume(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("重启jobName={},jobGroup={}", jobName, jobGroup);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            if (!checkExists(jobName, jobGroup)) {
                log.error("Job不存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job不存在, jobName={},jobGroup={}", jobName, jobGroup));
            }
            scheduler.resumeTrigger(triggerKey);
        } catch (SchedulerException e) {
            log.error("重启job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 立即执行
     *
     * @param jobName
     * @param jobGroup
     */
    public void trigger(String jobName, String jobGroup) throws BusinessException {
        try {
            log.info("立即执行jobName={},jobGroup={}", jobName, jobGroup);
            if (!checkExists(jobName, jobGroup)) {
                log.error("Job不存在, jobName={},jobGroup={}", jobName, jobGroup);
                throw new BusinessException(String.format("Job不存在, jobName={},jobGroup={}", jobName, jobGroup));
            }
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            log.error("立即执行job失败, jobName={},jobGroup={},e={}", jobName, jobGroup, e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 验证是否存在
     *
     * @param jobName
     * @param jobGroup
     * @throws SchedulerException
     */
    private boolean checkExists(String jobName, String jobGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        return scheduler.checkExists(triggerKey);
    }

}