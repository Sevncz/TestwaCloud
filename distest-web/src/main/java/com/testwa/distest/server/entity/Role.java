package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by wen on 2016/11/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="role")
public class Role extends BaseEntity {

    @Column(name = "roleName")
    private String roleName;
    @Column(name = "createTime")
    private Date createTime;
    @Column(name = "createBy")
    private Long createBy;
    @Column(name = "updateTime")
    private Date updateTime;
    @Column(name = "updateBy")
    private Long updateBy;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

}
