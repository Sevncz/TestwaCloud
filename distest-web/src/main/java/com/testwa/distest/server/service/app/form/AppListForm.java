package com.testwa.distest.server.service.app.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "AppListForm",
        description = "查询"
)
@Data
public class AppListForm extends RequestListBase {
    private String appName;
    private String packageName;
}
