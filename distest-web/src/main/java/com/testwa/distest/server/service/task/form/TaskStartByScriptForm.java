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
@ApiModel(value = "TaskStartByScriptForm",
        description = "选择脚本并启动测试"
)
@Data
public class TaskStartByScriptForm extends RequestFormBase {

    @NotNull
    private Long appId;
    @NotEmpty
    private List<String> scripts;
    @NotEmpty
    private List<String> devices;
    @NotNull
    private Long projectId;

}
