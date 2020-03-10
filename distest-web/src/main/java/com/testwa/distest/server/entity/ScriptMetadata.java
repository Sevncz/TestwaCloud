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
    @Column(name = "language")
    private DB.ScriptLN language;
    @Column(name = "strategy_key")
    private String strategyKey;
    @Column(name = "strategy_value")
    private String strategyValue;
    @Column(name = "strategy_desc")
    private String strategyDesc;
}
