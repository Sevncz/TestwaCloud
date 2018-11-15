package com.testwa.distest.server.service.device.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@ApiModel(value = "DeviceScopeRemoveForm",
        description = "移除一个设备分享对象"
)
@Data
public class DeviceScopeRemoveForm extends RequestFormBase {
    @NotBlank
    private String deviceId;
    @NotNull
    private Long userId;
}
