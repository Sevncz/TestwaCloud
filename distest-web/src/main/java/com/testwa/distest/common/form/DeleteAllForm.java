package com.testwa.distest.common.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yxin on 8/8/2017.
 */
@ApiModel(value = "DeleteAllForm",
        description = "删除多个"
)
@Data
public class DeleteAllForm extends RequestFormBase{
    @NotNull
    private List<Long> entityIds;
}
