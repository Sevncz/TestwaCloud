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
public class ScriptCondition extends BaseProjectCondition{
    @Condition(logic = Logic.LIKE)
    private String scriptName;
    private DB.ScriptLN ln;
    private String appPackage;
    private String md5;
}
