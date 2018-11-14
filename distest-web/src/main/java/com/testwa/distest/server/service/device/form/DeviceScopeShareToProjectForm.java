package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;


@ApiModel(value = "DeviceScopeShareToProjectForm",
        description = "设备分享对象"
)
@Data
public class DeviceScopeShareToProjectForm extends RequestFormBase {
    @NotEmpty
    private String deviceId;
    @NotEmpty
    private List<Long> toProjectIdList;
}
