package com.testwa.distest.server.service.script.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "ScriptUpdateForm",
        description = "更新"
)
@Data
public class ScriptUpdateForm extends ScriptNewForm{
    @NotEmpty
    private Long scriptId;

    private String content;
}
