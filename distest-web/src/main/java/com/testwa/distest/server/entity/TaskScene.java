package com.testwa.distest.server.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
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
@TableName("taskscene")
public class TaskScene extends BaseEntity {

    private String sceneName;
    private Long projectId;
    private Long appId;
    private String description;
    private DB.RunMode exeMode;

    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

    private List<Testcase> testcases;

}
