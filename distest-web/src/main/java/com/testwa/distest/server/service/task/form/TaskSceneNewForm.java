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
@ApiModel(value = "TaskSceneNewForm",
        description = "创建"
)
@Data
public class TaskSceneNewForm extends RequestFormBase {

    @NotNull(message = "sceneName.empty")
    private String sceneName;
    @NotNull(message = "projectId.empty")
    private Long projectId;
    @NotNull(message = "caseIds.empty")
    private List<Long> caseIds;
    @NotNull(message = "appId.empty")
    private Long appId;
    private String description;

}
