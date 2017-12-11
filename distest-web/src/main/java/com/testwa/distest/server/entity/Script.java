package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * Created by wen on 16/9/1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("script")
public class Script extends BaseEntity {

    private String scriptName;
    private String aliasName;
    private String appPackage;
    private String size;
    private String tag;
    private String description;
    private DB.ScriptLN ln;
    private String md5;
    private String path;
    private Long projectId;
    private Date updateTime;
    private Long updateBy;
    private Date createTime;
    private Long createBy;
    private Boolean enabled;

    @Column(value="createUser", ignore=true)
    private User createUser;
    @Column(value="updateUser", ignore=true)
    private User updateUser;

}
