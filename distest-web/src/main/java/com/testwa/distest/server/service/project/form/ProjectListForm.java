package com.testwa.distest.server.service.project.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "ProjectListForm",
        description = "查询"
)
@Data
public class ProjectListForm extends RequestListBase {
    private String projectName;
}
