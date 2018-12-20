package com.testwa.distest.server.condition;

import com.testwa.core.base.bo.BaseCondition;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-20 15:04
 */
@Data
public class BaseProjectCondition extends BaseCondition {
    private Long projectId;
    private Long createBy;
}
