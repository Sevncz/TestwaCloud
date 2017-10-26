package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@ToString
public class Task extends BaseEntity {

    private String taskName;
    private Long projectId;
    private Long appId;
    private String description;
    private DB.ExecutorMode exeMode;

    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

    private List<Testcase> testcases;

}
