package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;


@ApiModel(value = "DeviceScopeRemoveForm",
        description = "移除一个设备分享对象"
)
@Data
public class DeviceScopeRemoveForm extends RequestFormBase {
    private String deviceId;
    private Long userId;
}
