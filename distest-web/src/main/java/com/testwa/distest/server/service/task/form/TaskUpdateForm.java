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
@ApiModel(value = "TaskUpdateForm",
        description = "更新"
)
@Data
public class TaskUpdateForm extends TaskNewForm {


    @NotNull(message = "taskId.empty")
    @NotEmpty(message = "taskId.empty")
    private Long taskId;

}
