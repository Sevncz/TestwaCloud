package com.testwa.core.base.bo;

import lombok.Data;

/**
 * @author wen
 * @create 2018-12-18 17:52
 */
@Data
public class BaseCondition {
    private Long id;
    private boolean enabled;

    public BaseCondition() {
        this.enabled = true;
    }

}
