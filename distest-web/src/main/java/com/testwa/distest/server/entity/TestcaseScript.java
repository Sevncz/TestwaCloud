package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

/**
 * Created by wen on 21/10/2017.
 */
@Data
@TableName("testcase_script")
public class TestcaseScript extends BaseEntity{

    private Long testcaseId;
    private Long scriptId;
    private int seq;
}
