package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@Data
@TableName("taskscene_testcase")
public class TaskSceneTestcase extends BaseEntity{

    private Long testcaseId;
    private Long taskSceneId;
    private int seq;

}
