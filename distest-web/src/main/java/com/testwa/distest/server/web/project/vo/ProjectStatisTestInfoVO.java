package com.testwa.distest.server.web.project.vo;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectStatisTestInfoVO {

    private String testTime;
    private String testCount;
    private String debugTime;
    private String scriptNum;


}
