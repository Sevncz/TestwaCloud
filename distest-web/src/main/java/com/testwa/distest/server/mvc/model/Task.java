package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Document(collection = "t_task")
public class Task {
    @Id
    private String id;

    private String appId;

    private String caseId;

    private String deviceId;

    private String creator;

    @CreatedDate
    private Date createDate;

    private Date modifyDate;

}
