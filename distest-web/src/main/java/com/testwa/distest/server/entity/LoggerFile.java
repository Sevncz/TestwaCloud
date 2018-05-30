package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("logger_file")
public class LoggerFile extends BaseEntity {

    private Long taskCode;
    private String deviceId;
    private String filename;
    private Date createTime;

    /**
     * 返回url的相对路径
     * @return
     */
    public String buildPath(){
        return "/" + taskCode + "/" + deviceId + "/" + filename;
    }
}
