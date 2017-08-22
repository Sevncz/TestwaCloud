package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Document(collection = "t_task_testcase")
public class TaskTestcase {
    @Id
    private String id;


}
