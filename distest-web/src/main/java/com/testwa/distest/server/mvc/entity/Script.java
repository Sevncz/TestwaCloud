package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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

}
