package com.testwa.distest.server.errorhandler;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * Internal server error message.
     */
    private static final String ERR_INTERNAL_SERVER_ERROR = "Internal server error";

    @Autowired
    MessageSource messageSource;

    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleBadCredentialsExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NO_AUTH.getValue());
        r.setMessage("用户名密码错误");
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Result handleExpiredJwtExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.EXPRIED_TOKEN.getValue());
        r.setMessage("Token已过期");
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Result handleSignatureExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.ILLEGAL_TOKEN.getValue());
        r.setMessage("非法Token");
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = AuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Result handleAuthorizedExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NO_AUTH.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = LoginInfoNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Result handleLoginInfoNotFoundExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NO_LOGIN.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ParamsException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleParamsExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ObjectNotExistsException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleObjectNotFoundExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.NOT_FOUND.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = ObjectAlreadyExistException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleObjectAlreadyExistExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = DeviceNotActiveException.class)
    @ResponseBody
    public Result handleDeviceNotActiveExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage(e.getMessage());
        r.setUrl(req.getRequestURL().toString());
        return r;
    }

    @ExceptionHandler(value = DBException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleDBExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SERVER_ERROR.getValue());
        r.setMessage(ERR_INTERNAL_SERVER_ERROR);
        r.setUrl(req.getRequestURL().toString());
        log.error(e.getMessage());
        return r;
    }


    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleHttpMessageNotReadableExceptions(HttpServletRequest req, Exception e) throws Exception {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage("请求格式错误");
        r.setUrl(req.getRequestURL().toString());
        log.error(e.getMessage());
        return r;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleMethodArgumentNotValidExceptions(HttpServletRequest req, MethodArgumentNotValidException e) throws Exception {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(this::buildMessage)
                .collect(Collectors.toList());
        Result<String> r = new Result<>();
        r.setCode(ResultCode.PARAM_ERROR.getValue());
        r.setMessage(errors.toString());
        r.setUrl(req.getRequestURL().toString());
        log.error(e.getMessage());
        return r;
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleOtherExceptions(HttpServletRequest req, Exception e) throws Exception {
        log.error("Unknow error", e);
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SERVER_ERROR.getValue());
        r.setMessage(ERR_INTERNAL_SERVER_ERROR);
        r.setUrl(req.getRequestURL().toString());

        log.error(e.getMessage());
        return r;
    }

    private String buildMessage(FieldError fe) {
        StringBuilder errorCode = new StringBuilder("");
        String localizedErrorMsg = "";
        errorCode.append("error").append(".");
        errorCode.append(fe.getObjectName()).append(".");
        errorCode.append(fe.getField()).append(".");
        errorCode.append(fe.getCode().toLowerCase());

        try {
            localizedErrorMsg = this.messageSource.getMessage(String.valueOf(errorCode), (Object[]) null, LocaleContextHolder.getLocale());
        } catch (Exception ex) {
            localizedErrorMsg = fe.getDefaultMessage();
        }
        return localizedErrorMsg;
    }

}