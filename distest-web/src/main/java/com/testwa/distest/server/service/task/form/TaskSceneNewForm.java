package com.testwa.distest.server.service.task.form;

import com.testwa.distest.common.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskSceneNewForm",
        description = "创建"
)
@Data
public class TaskSceneNewForm extends RequestFormBase {

    @NotNull(message = "taskName.empty")
    @NotEmpty(message = "taskName.empty")
    private String taskName;
    @NotNull(message = "projectId.empty")
    @NotEmpty(message = "projectId.empty")
    private Long projectId;
    @NotNull(message = "caseIds.empty")
    @NotEmpty(message = "caseIds.empty")
    private List<Long> caseIds;
    @NotNull(message = "appId.empty")
    @NotEmpty(message = "appId.empty")
    private Long appId;
    private String description;

}
