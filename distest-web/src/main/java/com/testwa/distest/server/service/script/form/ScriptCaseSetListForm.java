package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 09/03/2020.
 */
@ApiModel(value = "ScriptCaseSetListForm",
        description = "查询"
)
@Data
public class ScriptCaseSetListForm extends RequestListBase {
    private String caseName;
}
