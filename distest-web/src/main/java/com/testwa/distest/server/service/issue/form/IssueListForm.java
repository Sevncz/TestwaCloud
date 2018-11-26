package com.testwa.distest.server.service.issue.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "IssueNewForm",
        description = "创建一个issue"
)
@Data
public class IssueListForm extends RequestListBase {
    private Long authorId;
    private Long assigneeId;
    private String labelName;
    private String state;
    private String issueSearch;
}
