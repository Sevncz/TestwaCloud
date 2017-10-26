package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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
