package com.testwa.distest.server.service.issue.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "IssueUpdateForm",
        description = "更新一个issue"
)
@Data
public class IssueUpdateForm extends RequestFormBase {

    @Length(min = 1, max = 100)
    private String title;
    private List<String> labelName;
    private List<Long> assigneeIds;
}
