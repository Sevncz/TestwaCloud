package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.io.File;
import java.util.Date;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("appium_file")
public class AppiumFile extends BaseEntity {

    private Long taskId;
    private String deviceId;
    private String filename;
    private Date createTime;

    /**
     * 返回url的相对路径
     * @return
     */
    public String buildPath(){
        return "/" + taskId + "/" + deviceId + "/" + filename;
    }
}
