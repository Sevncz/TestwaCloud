package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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