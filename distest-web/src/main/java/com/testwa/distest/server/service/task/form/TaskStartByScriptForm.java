package com.testwa.distest.server.service.task.form;

import com.testwa.distest.common.form.RequestFormBase;
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
    @NotEmpty
    private String appId;
    @NotNull
    @NotEmpty
    private List<String> scripts;
    @NotNull
    @NotEmpty
    private List<String> devices;
    @NotNull
    @NotEmpty
    private String projectId;

}
