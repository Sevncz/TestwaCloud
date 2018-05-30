package com.testwa.distest.server.mongo.model;import lombok.Data;import org.springframework.data.annotation.Id;import org.springframework.data.mongodb.core.index.Indexed;import org.springframework.data.mongodb.core.mapping.Document;/** * @Program: distest * @Description: monkey执行的点击步骤，主要是保存截图和任务的关系 * @Author: wen * @Create: 2018-05-21 21:49 **/@Data@Document(collection = "t_step")public class Step {    @Id    private String id;    @Indexed    private String deviceId;    @Indexed    private Long taskCode;    private String img;    private String dump;    private String action;    private Integer order;    private Integer status;    private Long runtime;    private String errormsg;    private Long timestamp;    /**     * 回归测试字段，appium返回的一部分内容     *     */    private Long testcaseId;    private Long scriptId;    private String commandAction;    private String commandParams;    private String value;    private String sessionId;    public static String getCollectionName() {        Document doc = Step.class.getAnnotation(Document.class);        return doc.collection();    }}