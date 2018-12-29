package com.testwa.distest.server.service.issue.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "IssueNewForm",
        description = "创建一个issue"
)
@Data
public class IssueNewForm extends RequestFormBase {
    @NotBlank
    @Length(min = 1, max = 256)
    private String title;
    @NotBlank
    @Length(min = 1)
    private String content;
    private Integer priority;
    private List<String> labelName;
    private Long assigneeId;
}
