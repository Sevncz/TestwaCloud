package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "ScriptContentForm",
        description = "更新"
)
@Data
public class ScriptContentForm extends RequestFormBase{
    @NotNull
    private String content;
}
