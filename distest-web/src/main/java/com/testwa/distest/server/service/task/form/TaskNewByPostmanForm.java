package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskNewByPostmanForm",
        description = "创建一个接口测试任务"
)
@Data
public class TaskNewByPostmanForm extends RequestFormBase {

    @NotNull
    private Long postmanCaseId;

}
