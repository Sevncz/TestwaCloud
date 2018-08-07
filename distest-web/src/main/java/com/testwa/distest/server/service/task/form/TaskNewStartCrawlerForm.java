package com.testwa.distest.server.service.task.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@ApiModel(value = "TaskNewForm",
        description = "创建一个遍历测试任务"
)
@Data
public class TaskNewStartCrawlerForm extends RequestFormBase {

    @NotNull
    @NotEmpty
    private Long projectId;
    @NotNull
    @NotEmpty
    private List<String> deviceIds;
    @NotNull
    @NotEmpty
    private Long appId;

    private List<String> blackList;
    private List<String> whiteList;
    private List<String> urlBlackList;
    private List<String> urlWhiteList;
    private List<String> firstList;
    private List<String> lastList;
    private List<String> backButton;
    private List<Map<String, Object>> trigger;
    private Integer maxDepth;

}
