package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;


@ApiModel(value = "DeviceScopeShareToProjectForm",
        description = "设备分享对象"
)
@Data
public class DeviceScopeShareToProjectForm extends RequestFormBase {
    private String deviceId;
    private List<Long> toProjectIdList;
}
