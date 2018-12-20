package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

@Data
@Table(name="dis_issue_label_dict")
public class IssueLabelDict extends BaseEntity {

    @Column(name = "name")
    private String name;
    @Column(name = "color")
    private String color;

}
