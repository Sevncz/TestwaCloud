package com.testwa.distest.server.condition;

import com.testwa.core.base.mybatis.annotation.Condition;
import com.testwa.core.base.mybatis.builder.Logic;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class ScriptCaseCondition extends BaseProjectCondition{
    @Condition(logic = Logic.LIKE)
    private String scriptCaseName;
    @Condition(logic = Logic.LIKE)
    private String appBasePackage;
    private String platform;
}
