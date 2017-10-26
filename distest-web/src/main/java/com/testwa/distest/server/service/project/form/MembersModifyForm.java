package com.testwa.distest.server.service.project.form;

import com.testwa.distest.common.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "MembersModifyForm",
        description = "批量修改项目成员，包括添加和删除"
)
@Data
@ToString
public class MembersModifyForm extends RequestFormBase{

    @NotNull
    private Long projectId;

    @NotNull
    private List<String> usernames;
}
