package com.testwa.distest.server.service.app.form;

import com.testwa.distest.common.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "AppNewForm",
        description = "新增app"
)
@Data
public class AppNewForm extends RequestFormBase{

    @NotNull
    private Long appId;
    @NotNull
    private Long projectId;
    private String version;
    private String description;
}
