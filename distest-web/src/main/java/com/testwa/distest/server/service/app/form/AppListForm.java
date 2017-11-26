package com.testwa.distest.server.service.app.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "AppListForm",
        description = "查询"
)
@Data
public class AppListForm extends RequestListBase {
    private Long projectId;
    private String appName;
}
