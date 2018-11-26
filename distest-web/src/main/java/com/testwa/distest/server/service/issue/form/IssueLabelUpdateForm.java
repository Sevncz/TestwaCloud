package com.testwa.distest.server.service.issue.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 22/11/2017.
 */
@ApiModel(value = "IssueLabelUpdateForm",
        description = "更新一个issuelabel"
)
@Data
public class IssueLabelUpdateForm extends IssueLabelNewForm {

    @NotNull
    private Long labelId;

}
