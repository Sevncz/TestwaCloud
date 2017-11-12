package com.testwa.distest.server.service.task.form;

import com.testwa.distest.common.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "TaskSceneListForm",
        description = "查询"
)
@Data
public class TaskSceneListForm extends RequestListBase {
    private Long projectId;
    private String sceneName;
    private Long appId;
}
