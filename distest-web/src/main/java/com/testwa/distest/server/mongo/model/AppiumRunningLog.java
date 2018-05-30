package com.testwa.distest.server.mongo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.distest.common.serializer.StringValueToLongSerializer;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wen on 16/8/27.
 */
@Data
@Document(collection = "t_appium_running_log")
public class AppiumRunningLog {
    @Id
    private String id;
    private Integer status;
    private String value;
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long runtime;
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long cpurate;
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long memory;
    private String battery;
    @Indexed
    private String sessionId;
    @Indexed
    private String deviceId;
    // 脚本ID
    @Indexed
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long testSuit;
    @Indexed
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long testcaseId;
    // 任务ID
    @Indexed
    @JSONField(serializeUsing = StringValueToLongSerializer.class)
    private Long taskCode;
    private String screenshotPath;
    private String description;
    private AppiumRunningCommand command;
    @CreatedDate
    private Date createDate;
    private Long timestamp;
    @JsonIgnore
    private Long userId;
    @JsonIgnore
    private String token;

    public static String getCollectionName() {
        Document doc = AppiumRunningLog.class.getAnnotation(Document.class);
        return doc.collection();
    }
}
