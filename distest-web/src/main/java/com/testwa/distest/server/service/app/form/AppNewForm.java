package com.testwa.distest.server.service.app.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

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
    private Long projectId;
    @NotBlank
    private String version;
    private String description;
}
