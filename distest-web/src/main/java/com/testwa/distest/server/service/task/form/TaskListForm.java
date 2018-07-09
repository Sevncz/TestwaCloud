package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "TaskListForm",
        description = "查询"
)
@Data
public class TaskListForm extends RequestListBase {
    private String taskName;
    private Long appId;
}
