package com.testwa.distest.server.service.task.form;

import com.testwa.distest.common.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 25/10/2017.
 */
@ApiModel(value = "TaskStopForm",
        description = "杀任务"
)
@Data
public class TaskStopForm extends RequestFormBase{


}
