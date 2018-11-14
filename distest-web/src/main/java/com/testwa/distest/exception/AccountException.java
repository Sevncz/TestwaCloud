package com.testwa.distest.exception;

import com.testwa.core.base.constant.ResultCode;

public class AccountException extends CommonBusinessException {
    private ResultCode code;

    public AccountException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public ResultCode getCode() {
        return this.code;
    }

}
