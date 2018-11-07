package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;


@ApiModel(value = "DeviceScopeShareToUserForm",
        description = "设备分享对象"
)
@Data
public class DeviceScopeShareToUserForm extends RequestFormBase {
    private String deviceId;
    private Set<Long> addUserId;
    private Set<Long> removeUserId;

    private Long startTime;
    private Long endTime;
}
