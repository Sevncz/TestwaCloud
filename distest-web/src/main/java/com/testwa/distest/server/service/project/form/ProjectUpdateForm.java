package com.testwa.distest.server.service.project.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "ProjectUpdateForm",
        description = "更新一个项目"
)
@Data
public class ProjectUpdateForm extends ProjectNewForm {
    @NotNull
    private Long projectId;
}
