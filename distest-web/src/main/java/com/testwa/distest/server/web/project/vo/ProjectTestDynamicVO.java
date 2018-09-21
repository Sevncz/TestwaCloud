package com.testwa.distest.server.web.project.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectTestDynamicVO {

    private String appName;
    private String testType;
    private String user;
    private Integer equipments;
    private Date time;
    private Integer status;

}
