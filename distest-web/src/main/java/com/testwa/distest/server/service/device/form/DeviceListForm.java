package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@ApiModel(value = "DeviceListForm",
        description = "查询"
)
@Data
public class DeviceListForm extends RequestListBase {
    private Long projectId;
    private String deviceId;
    private String brand;
    private String model;
}
