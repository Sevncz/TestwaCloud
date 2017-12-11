package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@Data
@TableName("testcase_detail")
public class TestcaseDetail extends BaseEntity implements Comparable<TestcaseDetail>{

    private Long testcaseId;
    private Long scriptId;
    private int seq;

    @Column(value="script", ignore=true)
    private Script script;
    @Column(value="testcase", ignore=true)
    private Testcase testcase;

    @Override
    public int compareTo(@NotNull TestcaseDetail o) {
        return this.seq - o.seq;
    }
}
