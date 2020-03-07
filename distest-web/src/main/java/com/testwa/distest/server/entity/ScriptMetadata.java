package com.testwa.distest.server.entity;

import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.distest.common.enums.DB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "dis_script_metadata")
public class ScriptMetadata extends BaseEntity {
    @Column(name = "strategy")
    private String strategy;
    @Column(name = "language")
    private DB.ScriptLN language;
    @Column(name = "strategy_name")
    private String strategyName;
    @Column(name = "strategy_desc")
    private String strategyDesc;
}
