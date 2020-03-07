package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="dis_script_case")
public class ScriptCase extends ProjectBase {

    /**
     * 脚本案例ID
     */
    @Column(name = "script_case_id")
    private String scriptCaseId;

}
