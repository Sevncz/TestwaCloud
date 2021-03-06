package com.testwa.distest.server.condition;

import com.testwa.core.base.mybatis.annotation.Condition;
import com.testwa.core.base.mybatis.builder.Logic;
import lombok.Data;

import java.util.List;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class IssueLabelCondition extends BaseProjectCondition{

    private String color;
    @Condition(logic = Logic.IN)
    private List<String> name;
}
