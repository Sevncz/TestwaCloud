package com.testwa.distest.server.exception;

import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.error("Unknow error", e);
        Result<String> r = new Result<>();
        if (e instanceof RuntimeException) {
            r.setCode(ResultCode.SERVER_ERROR.getValue());
            r.setData("Some error in server.");
        } else {
            r.setCode(ResultCode.INVALID_PARAM.getValue());
            r.setData(e.getClass().getCanonicalName());
        }
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());

        return r;
    }

}