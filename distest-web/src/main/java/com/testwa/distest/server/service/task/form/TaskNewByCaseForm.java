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
@ApiModel(value = "TaskNewForm",
        description = "创建一个任务"
)
@Data
public class TaskNewByCaseForm extends RequestFormBase {

    @NotNull(message = "testcaseId.empty")
    @NotEmpty(message = "testcaseId.empty")
    private Long testcaseId;
    private String description;

}