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
        description = "创建一个兼容测试任务"
)
@Data
public class TaskNewStartCompatibilityForm extends RequestFormBase {

    @NotEmpty
    private Long projectId;
    @NotEmpty
    private List<String> deviceIds;
    @NotEmpty
    private Long appId;

}
