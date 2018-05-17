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
@ApiModel(value = "TaskStartForm",
        description = "启动一个已存在的场景"
)
@Data
public class TaskStartForm extends RequestFormBase {

    private List<String> deviceIds;

    private Long projectId;

}
