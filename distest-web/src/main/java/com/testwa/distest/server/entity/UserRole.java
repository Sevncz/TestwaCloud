package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * Created by wen on 23/10/2017.
 */
@Data
@TableName("user_role")
public class UserRole extends BaseEntity{

    private Long userId;
    private Long roleId;

}
