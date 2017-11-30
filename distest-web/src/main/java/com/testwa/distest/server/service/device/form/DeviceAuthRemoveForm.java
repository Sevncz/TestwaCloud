package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;


@ApiModel(value = "DeviceAuthRemoveForm",
        description = "从某设备中移除一个或多个用户的使用权"
)
@Data
public class DeviceAuthRemoveForm extends RequestFormBase {
    @NotNull(message="用户不能为空")
    @NotEmpty(message="用户不能为空")
    private List<Long> userIds;
    @NotNull(message="设备不能为空")
    @NotEmpty(message="设备不能为空")
    private String deviceId;
}
