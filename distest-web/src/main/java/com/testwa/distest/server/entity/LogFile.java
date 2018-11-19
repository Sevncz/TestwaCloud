package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("log_file")
public class LogFile extends BaseEntity {

    @Column(value = "task_code")
    private Long taskCode;
    @Column(value = "device_id")
    private String deviceId;
    @Column(value = "file_name")
    private String filename;
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 返回url的相对路径
     * @return
     */
    public String buildPath(){
        return "/" + taskCode + "/" + deviceId + "/" + filename;
    }
}
