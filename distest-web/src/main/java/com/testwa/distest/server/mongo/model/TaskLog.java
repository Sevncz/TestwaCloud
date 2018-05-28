package com.testwa.distest.server.mongo.model;import lombok.Data;import org.springframework.data.annotation.Id;import org.springframework.data.mongodb.core.index.Indexed;import org.springframework.data.mongodb.core.mapping.Document;/** * @Program: distest * @Description: 测试任务对应的日志 * @Author: wen * @Create: 2018-05-16 15:25 **/@Data@Document(collection = "t_task_log")public class TaskLog {    @Id    private String id;    @Indexed    private String deviceId;    @Indexed    private Long taskId;    // logcat日志分析完之后的内容    private String content;    // 错误数量    private Integer errorNum;    private Long timestamp;    public static String getCollectionName() {        Document doc = TaskLog.class.getAnnotation(Document.class);        return doc.collection();    }}