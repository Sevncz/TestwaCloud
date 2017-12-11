package com.testwa.distest.server.service.task.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskSceneUpdateForm",
        description = "更新"
)
@Data
public class TaskSceneUpdateForm extends TaskSceneNewForm {


    @NotNull(message = "taskSceneId.empty")
    private Long taskSceneId;

}
