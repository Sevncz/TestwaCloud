package com.testwa.distest.exception;

import com.testwa.core.base.constant.ResultCode;

public class JobException extends CommonBusinessException {
    private ResultCode code;

    public JobException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public ResultCode getCode() {
        return this.code;
    }
}
