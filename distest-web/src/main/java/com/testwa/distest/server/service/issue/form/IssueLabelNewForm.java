package com.testwa.distest.server.service.issue.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 22/11/2017.
 */
@ApiModel(value = "IssueLabelNewForm",
        description = "创建一个issuelabel"
)
@Data
public class IssueLabelNewForm extends RequestFormBase {
    @NotBlank
    @Length(min = 1, max = 20)
    private String name;
    @NotBlank
    @Length(min = 6, max = 6)
    private String color;
}
