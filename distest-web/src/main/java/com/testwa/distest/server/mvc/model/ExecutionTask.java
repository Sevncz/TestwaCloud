package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Document(collection = "t_execution_task")
public class ExecutionTask {
    @Id
    private String id;

    private String taskId;

    private App app;

    private List<TDevice> devices;

    private String projectId;

    private List<Testcase> testcases;

    private Map<String, List<Script>> scripts;

    private Integer status;

    private String creator;

    @CreatedDate
    private Date createDate;

    private Date modifyDate;


}
