package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;


@Data
@Table(name="role_resource")
public class RoleResource extends BaseEntity {

    @Column(name = "roleId")
    private Long roleId;

    @Column(name = "urlPattern")
    private String urlPattern;

    @Column(name = "urlDescription")
    private String urlDescription;

    @Column(name = "methodMask")
    private String methodMask;

    @Column(name = "updateTime")
    private Date updateTime;




}