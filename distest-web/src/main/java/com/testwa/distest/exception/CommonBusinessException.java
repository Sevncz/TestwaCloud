package com.testwa.distest.exception;

import com.testwa.core.base.constant.ResultCode;

public abstract class CommonBusinessException extends RuntimeException {

    public CommonBusinessException(String message) {
        super(message);
    }


    public abstract ResultCode getCode();
}
