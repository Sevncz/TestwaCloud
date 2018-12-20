package com.testwa.distest.server.condition;

import com.testwa.core.base.mybatis.annotation.Condition;
import com.testwa.core.base.mybatis.builder.Logic;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class TestcaseCondition extends BaseProjectCondition{
    private Long appInfoId;
    private String tag;
    @Condition(logic = Logic.LIKE)
    private String caseName;
    @Condition(logic = Logic.LIKE)
    private String packageName;
    @Condition(logic = Logic.LIKE)
    private String appName;
}
