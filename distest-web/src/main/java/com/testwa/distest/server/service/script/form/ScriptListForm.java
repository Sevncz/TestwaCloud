package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "ScriptListForm",
        description = "查询"
)
@Data
public class ScriptListForm extends RequestListBase {
    private String scriptName;
    private String packageName;
    private Integer ln;
}
