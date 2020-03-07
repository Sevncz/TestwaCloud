package com.testwa.distest.server.entity;

import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 测试动作
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="dis_script_action")
public class ScriptAction extends ProjectBase {

    /**
     * 脚本案例ID
     */
    @Column(name = "script_case_id")
    private Long scriptCaseId;
    /**
     * 方法uuid
     */
    @Column(name = "function_id")
    private String functionId;
    /**
     * 动作：findAndAssign, click, back, tap, sendKeys 等
     */
    @Column(name = "action")
    private String action;
    /**
     * 参数，JSON格式
     */
    @Column(name = "parameter")
    private String parameter;

}
