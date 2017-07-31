package com.testwa.distest.server.exception;

import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.error("Unknow error", e);
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SERVER_ERROR.getValue());
        r.setData("Some error in server.");
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());

        return r;
    }

}