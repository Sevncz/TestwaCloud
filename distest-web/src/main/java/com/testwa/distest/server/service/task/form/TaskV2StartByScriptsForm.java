package com.testwa.distest.server.service.task.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 10/03/2020.
 */
@Data
public class TaskV2StartByScriptsForm extends TaskNewStartCompatibilityForm {

    @NotNull
    private List<String> scriptCaseIds;

}
