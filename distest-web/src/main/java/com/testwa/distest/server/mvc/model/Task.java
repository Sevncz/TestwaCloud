package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Document(collection = "t_task")
public class Task {
    @Id
    private String id;

    private String appId;

    private String name;

    private String projectId;

    private List<String> testcaseIds;

    private String creator;

    private boolean disable;

    private Long createDate;

    private Long  modifyDate;


}
