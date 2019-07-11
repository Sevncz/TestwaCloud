package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * Created by wen on 23/10/2017.
 */
@Data
@Table(name="user_role")
public class UserRole extends BaseEntity{

    @Column(name="userId")
    private Long userId;
    @Column(name="roleId")
    private Long roleId;



}
