package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

/**
 * issue 和 label 的对应关系表
 *
 * @author wen
 * @create 2018-12-21 10:40
 */
@Data
@Table(name="dis_issue_label_map")
public class IssueLabelMap extends BaseEntity {
    private Long issueId;
    private Long labelId;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;
}
