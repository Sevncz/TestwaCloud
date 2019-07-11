package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Table(name="dis_appium_file")
public class AppiumFile extends BaseEntity {

    @Column(name = "task_code")
    private Long taskCode;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "filename")
    private String filename;
    @Column(name = "create_time")
    private Date createTime;



    /**
     * 返回url的相对路径
     * @return
     */
    public String buildPath(){
        return "/" + taskCode + "/" + deviceId + "/" + filename;
    }
}
