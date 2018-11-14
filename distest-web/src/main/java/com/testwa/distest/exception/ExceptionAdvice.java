package com.testwa.distest.exception;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice{
    private static final String HANDLE_EXCEPTION_TEMPLATE = "handle {},url:{},caused by:";
    /**
     * dto 参数校验异常处理
     *
     * @param e 校验异常
     * @return result
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        return parseBindingResult(e.getBindingResult());
    }

    @ExceptionHandler(value = BindException.class)
    protected Result handleBindException(BindException e, HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        return parseBindingResult(bindingResult);
    }

    /**
     * parameter 参数校验异常处理
     *
     * @param e 校验异常
     * @return result
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
        if (iterator.hasNext()) {
            ConstraintViolation<?> next = iterator.next();
            Path propertyPath = next.getPropertyPath();
            String name = "unknown";
            for (Path.Node node : propertyPath) {
                name = node.getName();
            }
            String message = "[" + name + "]" + next.getMessage();
            return Result.error(ResultCode.INVALID_PARAM, message);
        }
        return Result.error(ResultCode.INVALID_PARAM);
    }

    /**
     * parameter 参数校验异常处理
     *
     * @param e 校验异常
     * @return result
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleConstraintViolationException(HttpMessageNotReadableException e, HttpServletRequest request) {
        return Result.error(ResultCode.INVALID_PARAM, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    protected Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logWithTemplate("RuntimeException", request, e);
        return Result.error(ResultCode.SERVER_ERROR, e.getMessage());
    }

    /**
    * 处理业务异常，所有业务异常都应该实现 CommonBusinessException 类
    **/
    @ExceptionHandler(CommonBusinessException.class)
    protected Result handleBusinessException(CommonBusinessException e, HttpServletRequest request) {
        log.info(HANDLE_EXCEPTION_TEMPLATE, e.getClass().getSimpleName(), request.getRequestURI(), e.getClass().getSimpleName());
        return Result.error(e.getCode(), e.getMessage());
    }
    private Result parseBindingResult(BindingResult bindingResult) {
        List<FieldError> errors = bindingResult.getFieldErrors();
        if (!errors.isEmpty()) {
            // 仅获取第一个异常
            FieldError next = errors.get(0);
            String name = next.getField();
            String message = next.getDefaultMessage();
            message = "[" + name + "]" + message;
            return Result.error(ResultCode.INVALID_PARAM, message);
        }
        return Result.error(ResultCode.INVALID_PARAM);
    }

    private void logWithTemplate(String exceptionName, HttpServletRequest request, Throwable e) {
        log.info(HANDLE_EXCEPTION_TEMPLATE, exceptionName, request.getRequestURI(), e);
    }
}