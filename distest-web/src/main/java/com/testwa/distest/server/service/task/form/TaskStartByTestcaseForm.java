package com.testwa.distest.server.service.task.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskStartByTestcaseForm",
        description = "新建场景并启动测试"
)
@Data
public class TaskStartByTestcaseForm extends TaskSceneNewForm {

    @NotNull(message = "deviceIds.empty")
    @NotEmpty(message = "deviceIds.empty")
    private List<String> deviceIds;


}
