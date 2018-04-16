package com.testwa.distest.account.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;


@Data
@TableName("role_resource")
public class RoleResource extends BaseEntity {

    private Long roleId;

    private String urlPattern;

    private String urlDescription;

    private String methodMask;

    private Date updateTime;


}