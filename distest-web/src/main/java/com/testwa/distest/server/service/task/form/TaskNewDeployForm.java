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
@ApiModel(value = "TaskNewDeployForm",
        description = "新建并开始"
)
@Data
public class TaskNewDeployForm extends TaskNewForm {

    @NotNull(message = "deviceIds.empty")
    @NotEmpty(message = "deviceIds.empty")
    private List<String> deviceIds;


}
