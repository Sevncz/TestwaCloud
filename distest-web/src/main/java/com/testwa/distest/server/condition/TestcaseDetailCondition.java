package com.testwa.distest.server.condition;

import com.testwa.core.base.bo.BaseCondition;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 18:29
 */
@Data
public class TestcaseDetailCondition extends BaseCondition {
    private Long testcaseId;
    private Long scriptId;
}
