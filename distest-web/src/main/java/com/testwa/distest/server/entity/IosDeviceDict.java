package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * @Program: distest
 * @Description: ios 设备字典表
 * @Author: wen
 * @Create: 2018-06-25 15:58
 **/
@Data
@TableName("ios_device_dict")
public class IosDeviceDict extends BaseEntity {
    @Column(value = "product_type")
    private String productType;
    @Column(value = "name")
    private String name;
    @Column(value = "width")
    private Integer width;
    @Column(value = "height")
    private Integer height;
}
