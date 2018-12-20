package com.testwa.distest.server.condition;

import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class IssueLabelCondition extends BaseProjectCondition{
    private String name;
    private String color;
}
