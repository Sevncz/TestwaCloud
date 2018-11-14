package com.testwa.distest.server.service.task.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Data
public class TaskNewStartByScriptsForm extends TaskNewStartCompatibilityForm {

    @NotEmpty
    private List<Long> scriptIds;

}
