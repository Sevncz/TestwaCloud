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
@ApiModel(value = "IssueCommentNewForm",
        description = "创建一个issue评论"
)
@Data
public class IssueCommentNewForm extends RequestFormBase {
    @NotBlank
    @Length(min = 1)
    private String content;
}
