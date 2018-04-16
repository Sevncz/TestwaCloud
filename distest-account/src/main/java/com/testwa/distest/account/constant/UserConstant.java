package com.testwa.distest.account.constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConstant {

    private static final Logger log = LoggerFactory.getLogger(UserConstant.class);

    /**
     * 是白名单用户
     */
    public static final int IS_WHITE_USER = 1;

    /**
     * 不是白名单用户
     */
    public static final int IS_NOT_WHITE_USER = 0;

    /**
     * 在redis中缓存登录token信息的key
     */
    public static final String USER_KEY = "user.key.";

    /**
     * 系统更新的数据统一用这个
     */
    public static final String USER_SYSTEM = "SYSTEM";

    //
    //
    //
    public static final String USER_APP_SEP = ",";

}