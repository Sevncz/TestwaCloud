package com.testwa.distest.server.service.task.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Data
public class TaskNewStartByScriptsForm extends TaskNewStartJRForm {

    @NotNull
    @NotEmpty
    private List<Long> scriptIds;

}
