package com.testwa.distest.quartz;

import lombok.Data;

import java.util.Date;

@Data
public class JobInfoVO {

    private String jobName;
    private String jobGroup;
    private String jobDescription;
    private String jobStatus;
    private String cronExpression;
    private String createTime;

    private Date previousFireTime;
    private Date nextFireTime;

}