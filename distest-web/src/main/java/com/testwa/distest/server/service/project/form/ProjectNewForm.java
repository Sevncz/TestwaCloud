package com.testwa.distest.server.service.project.form;

import com.testwa.distest.common.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "ProjectNewForm",
        description = "创建一个项目"
)
@Data
public class ProjectNewForm extends RequestFormBase {
    @NotNull
    private String projectName;
    private String description;
    private List<String> members;
}
