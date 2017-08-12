package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
@Data
@Document(collection = "t_testcase")
public class Testcase {
    @Id
    private String id;

    private List<String> scripts;

    private String userId;
    private String userName;

    @CreatedDate
    private Date createDate = new Date();

    private String name;

    private String projectId;
    private String projectName;

    private Boolean disable = false;

    private Date modifyDate;

}
