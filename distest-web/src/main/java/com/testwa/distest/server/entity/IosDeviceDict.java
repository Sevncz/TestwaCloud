package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * @Program: distest
 * @Description: ios 设备字典表
 * @Author: wen
 * @Create: 2018-06-25 15:58
 **/
@Data
@Table(name="dis_ios_device_dict")
public class IosDeviceDict extends BaseEntity {
    @Column(name = "product_type")
    private String productType;
    @Column(name = "name")
    private String name;
    @Column(name = "width")
    private Integer width;
    @Column(name = "height")
    private Integer height;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;
}
