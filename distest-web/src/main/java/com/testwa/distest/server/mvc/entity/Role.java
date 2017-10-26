package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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
@TableName("role")
public class Role extends BaseEntity {

    private String roleName;
    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;

}
