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
public class IssueCondition extends BaseProjectCondition{
    private Long labelId;
    private Long assigneeId;
    private Long authorId;
    @Condition(logic = Logic.LIKE)
    private String title;
    @Condition(logic = Logic.LIKE)
    private String content;
    private DB.IssueStateEnum state;
}
