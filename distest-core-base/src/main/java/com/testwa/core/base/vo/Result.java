package com.testwa.core.base.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.testwa.core.base.constant.ResultCode;
import lombok.Data;

/**
 * Created by wen on 2016/11/19.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 返回数据
     */
    private T data;
    /**
     * 错误码
     */
    private int code = 0;
    /**
     * 成功 / 失败标识
     */
    private boolean success = true;
    /**
     * 失败信息：用于前端 /api 调用者调试接口
     */
    private String message;
    private String url;

    public static <T> Result<T> success(T result) {
        Result<T> response = new Result<>();
        response.data = result;
        return response;
    }

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static Result error() {
        Result result = new Result<>();
        result.code = ResultCode.SERVER_ERROR.getValue();
        result.message = ResultCode.SERVER_ERROR.name();
        result.success = false;
        return result;
    }


    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(ResultCode code, String message) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = code.getValue();
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(ResultCode code) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = code.getValue();
        return result;
    }

}
