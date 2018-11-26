package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

@Data
@TableName("dis_issue_label_dict")
public class IssueLabelDict extends BaseEntity {

    @Column(value = "name")
    private String name;
    @Column(value = "color")
    private String color;

}
