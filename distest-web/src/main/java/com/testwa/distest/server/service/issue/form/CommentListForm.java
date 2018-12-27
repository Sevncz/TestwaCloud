package com.testwa.distest.server.service.issue.form;

import com.testwa.core.base.form.RequestListBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by wen on 20/10/2017.
 */
@ApiModel(value = "CommentListForm",
        description = "issue 评论列表"
)
@Data
public class CommentListForm extends RequestListBase {
}
