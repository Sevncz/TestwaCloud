package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "ScriptNewForm",
        description = "创建"
)
@Data
public class ScriptNewForm extends RequestFormBase{
    @NotNull(message = "projectId.empty")
    private Long projectId;
    private String tag;
    private String description;
}
