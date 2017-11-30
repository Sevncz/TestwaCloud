package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 26/10/2017.
 */
@Data
@NoArgsConstructor
@TableName("device")
public class Device extends BaseEntity {

    private String deviceId;
    private DB.PhoneOnlineStatus onlineStatus;
    private DB.PhoneOS phoneOS;
    /**注册时间**/
    private Date createTime = new Date();
    /**更新时间**/
    private Date updateTime = new Date();

    private String model; // 型号  Nexus 6
    private String brand; // 品牌 google

    private Long lastUserId;
    private String lastUserToken;

    private List<DeviceAuth> deviceAuths;

    protected Device(DB.PhoneOS phoneOS) {
        this.phoneOS = phoneOS;
    }

}
