package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;


@ApiModel(value = "DeviceAuthListForm",
        description = ""
)
@Data
public class DeviceAuthListForm extends RequestListBase {
    private Long userId;
    private String deviceId;
}
