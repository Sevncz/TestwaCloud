package com.testwa.core.base.exception.handler;

import com.testwa.core.base.vo.Result;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Log4j2
@ControllerAdvice
class GlobalExceptionHandler {
    /**
     * Internal server error message.
     */
    private static final String ERR_INTERNAL_SERVER_ERROR = "Internal server error";

    @ExceptionHandler(value = AuthorizedException.class)
    @ResponseBody
    public Result handleAuthorizedExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NO_AUTH.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ParamsException.class)
    @ResponseBody
    public Result handleParamsExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ObjectNotExistsException.class)
    @ResponseBody
    public Result handleObjectNotFoundExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NOT_FOUND.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ObjectAlreadyExistException.class)
    @ResponseBody
    public Result handleObjectAlreadyExistExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.CONFLICT.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = DBException.class)
    @ResponseBody
    public Result handleDBExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SERVER_ERROR.getValue());
        r.setMessage(ERR_INTERNAL_SERVER_ERROR);
        r.setUrl(req.getRequestURL().toString());
        log.error(e.getMessage());
        return r;
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result handleOtherExceptions(HttpServletRequest req, Exception e) throws Exception {
        log.error("Unknow error", e);
        Result<String> r = new Result<>();
        if (e instanceof RuntimeException) {
            r.setCode(ResultCode.SERVER_ERROR.getValue());
        }
        r.setMessage(ERR_INTERNAL_SERVER_ERROR);
        r.setUrl(req.getRequestURL().toString());

        log.error(e.getMessage());
        return r;
    }

}