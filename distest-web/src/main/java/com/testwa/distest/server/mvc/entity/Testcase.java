package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
@Data
@TableName("testcase")
public class Testcase extends BaseEntity {
    private String tag;
    private String caseName;
    private Long projectId;
    private String description;
    private DB.ExecutorMode exeMode;

    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;
    private Boolean enabled;

    private List<Script> scripts;
}
