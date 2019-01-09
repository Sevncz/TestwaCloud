package com.testwa.distest.server.service.project.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;


/**
 * @author wen
 * @create 2019-01-09 17:20
 */
@ApiModel(value = "MemberRoleUpdateForm",
        description = "修改成员角色"
)
@Data
@ToString
public class MemberRoleUpdateForm extends RequestFormBase {
    @NotNull
    private Long userId;
    @NotNull
    private Integer roleId;
}
