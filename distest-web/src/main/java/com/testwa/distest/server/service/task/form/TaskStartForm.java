package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskStartForm",
        description = "部署一个已存在的task"
)
@Data
public class TaskStartForm extends RequestFormBase {

    @NotNull(message = "taskSceneId.empty")
    @NotEmpty(message = "taskSceneId.empty")
    private Long taskSceneId;
    @NotNull(message = "deviceIds.empty")
    @NotEmpty(message = "deviceIds.empty")
    private List<String> deviceIds;

}
