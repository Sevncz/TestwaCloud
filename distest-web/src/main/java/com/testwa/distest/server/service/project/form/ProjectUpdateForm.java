package com.testwa.distest.server.service.project.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "ProjectUpdateForm",
        description = "更新一个项目"
)
@Data
public class ProjectUpdateForm extends ProjectNewForm {
    @NotEmpty
    private Long projectId;
}
