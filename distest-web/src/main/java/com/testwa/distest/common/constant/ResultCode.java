package com.testwa.distest.common.constant;

/**
 * Created by wen on 2016/11/19.
 *
 *   参照:
 *   ret = 0: 正确返回
 *   ret > 0: 调用OpenAPI时发生错误，需要开发者进行相应的处理。
 *   -50 <= ret <= -1: 接口调用不能通过接口代理机校验，需要开发者进行相应的处理。
 *   ret <-50: 系统内部错误，请通过联系技术支持，调查问题原因并获得解决方案。
 *
 */
public enum ResultCode {
    SUCCESS(0),

    PARAM_ERROR(1),
    NOT_FOUND(2),  // 查询对象不存在
    CONFLICT(3),  // 操作冲突
    ILLEGAL_OP(100), // 非法操作，通常是进行了不被授权的操作
    NO_LOGIN(101), // 没有登录信息
    ACCOUNT_FREEZE(102), //账户冻结

    INVALID_PARAM(-1), //无效参数
    NO_AUTH(-2), //无API访问权限。
    INVALID_IP(-3), //IP没有权限。
    OVERLOCK(-4), //超过访问频率。
    ILLEGAL_TOKEN(-5), //非法token。
    EXPRIED_TOKEN(-6), //token过期。
    NO_API(-10), //API不存在。
    SERVER_ERROR(-500), // 服务器内部错误
    ;

    private int value;

    ResultCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
