package com.testwa.distest.client.boost.impl;

import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/20.
 */
@Component
public class InstallAppCallbackImpl implements TestwaNotificationCallback {


    @Override
    public void done(Object o, TestwaException e) throws TestwaException {
        /**
         * 安装app的流程:
         * 1. 接受到安装app的请求
         * 2. 从服务器下载app(异步)
         * 3. 通过adb安装(异步)
         */
    }
}
