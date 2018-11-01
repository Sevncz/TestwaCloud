package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@ApiModel(value = "DeviceScopeNewForm",
        description = "设备分享范围配置"
)
@Data
public class DeviceScopeNewForm extends RequestFormBase {
    private String deviceId;
    private Integer scope;
}
