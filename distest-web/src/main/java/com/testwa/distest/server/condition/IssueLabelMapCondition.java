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
public class IssueLabelMapCondition extends BaseProjectCondition{

    private Long labelId;
    private Long issueId;
}
