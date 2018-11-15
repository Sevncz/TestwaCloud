package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 25/10/2017.
 */
@ApiModel(value = "TaskStopForm",
        description = "杀任务"
)
@Data
public class TaskStopForm extends RequestFormBase{

    @NotNull
    private Long taskCode;
    private List<String> deviceIds;
}
