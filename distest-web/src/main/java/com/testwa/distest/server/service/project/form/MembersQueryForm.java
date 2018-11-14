package com.testwa.distest.server.service.project.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "MembersQueryForm",
        description = "项目成员查询"
)
@Data
@ToString
public class MembersQueryForm extends RequestFormBase{

    @NotNull
    private Long projectId;

    private String username;
    private String email;
    private String phone;
}
