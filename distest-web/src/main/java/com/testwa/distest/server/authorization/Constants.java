package com.testwa.distest.server.authorization;

/**
 * Created by wen on 2016/11/12.
 */
public class Constants {

    /**
     * 存储当前登录用户id的字段名
     */
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    public static final String AUTHORIZATION = "authorization";

    /**
     * header中验证码key
     */
    public static final String CAPTCHA = "captcha";

    /**
     * cookie 中验证码 key
     */
    public final static String CAPTCHACODE = "CaptchaCode";

    /**
     * cookie 中 token key
     */
    public final static String TOKENCODE = "TokenCode";

    public final static String TOKEN_ISS = "TESTWA_AUTH";
}
