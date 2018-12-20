package com.testwa.distest.server.condition;

import com.testwa.core.base.bo.BaseCondition;
import com.testwa.core.base.mybatis.annotation.Condition;
import com.testwa.core.base.mybatis.builder.Logic;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class ProjectCondition extends BaseCondition {
    @Condition(logic = Logic.LIKE)
    private String projectName;
    private Long createBy;
}
