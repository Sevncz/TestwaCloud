package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 项目表
 * Created by wen on 16/8/30.
 */
@Data
@TableName("project")
public class Project extends BaseEntity {

    private String projectName;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

}
