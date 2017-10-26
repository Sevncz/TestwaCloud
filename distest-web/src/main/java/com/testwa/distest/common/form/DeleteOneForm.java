package com.testwa.distest.common.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yxin on 8/8/2017.
 */
@ApiModel(value = "DeleteOneForm",
        description = "删除一个对象"
)
@Data
public class DeleteOneForm extends RequestFormBase{
    @NotNull
    public Long entityId;
}
