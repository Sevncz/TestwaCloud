package com.testwa.distest.server.service.script.form;

import com.testwa.distest.common.form.RequestListBase;
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
    private Long projectId;
    private String scriptName;
    private String ln;
}
