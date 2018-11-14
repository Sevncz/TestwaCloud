package com.testwa.distest.exception;

import com.testwa.core.base.constant.ResultCode;

public class AuthorizedException extends CommonBusinessException {
    private ResultCode code;

    public AuthorizedException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public ResultCode getCode() {
        return this.code;
    }
}
