package com.testwa.distest.server.service.task.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskNewForm",
        description = "创建一个回归测试任务"
)
@Data
public class TaskNewByCaseAndStartForm extends TaskNewByCaseForm {

    @NotEmpty
    private List<String> deviceIds;
    @NotNull
    private Long appId;

}
