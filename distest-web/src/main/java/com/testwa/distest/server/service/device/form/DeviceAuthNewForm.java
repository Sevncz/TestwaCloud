package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;


@ApiModel(value = "DeviceAuthNewForm",
        description = "允许某个用户使用某设备"
)
@Data
public class DeviceAuthNewForm extends RequestFormBase {
    @NotNull(message="用户不能为空")
    @NotEmpty(message="用户不能为空")
    private List<Long> userIds;
    @NotNull(message="设备不能为空")
    @NotEmpty(message="设备不能为空")
    private String deviceId;
}
