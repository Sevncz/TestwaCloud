package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Transient;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 21/10/2017.
 */
@Data
@Table(name="testcase_detail")
public class TestcaseDetail extends BaseEntity implements Comparable<TestcaseDetail>{
    @Column(name="testcaseId")
    private Long testcaseId;
    @Column(name="scriptId")
    private Long scriptId;
    @Column(name="seq")
    private int seq;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

    @Transient
    private Script script;
    @Transient
    private Testcase testcase;

    @Override
    public int compareTo(@NotNull TestcaseDetail o) {
        return this.seq - o.seq;
    }
}
