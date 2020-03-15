package com.testwa.distest.server.service.task.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 10/03/2020.
 */
@Data
public class TaskV2StartByScriptSetForm extends TaskNewStartCompatibilityForm {

    @NotNull
    private String scriptCaseSetId;

}
