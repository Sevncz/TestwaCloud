package com.testwa.distest.server.condition;

import com.testwa.core.base.mybatis.annotation.Condition;
import com.testwa.core.base.mybatis.builder.Logic;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class ApiCondition extends BaseProjectCondition{
    private Long categoryId;
    private String method;
    @Condition(logic = Logic.LIKE)
    private String categoryPath;
}
