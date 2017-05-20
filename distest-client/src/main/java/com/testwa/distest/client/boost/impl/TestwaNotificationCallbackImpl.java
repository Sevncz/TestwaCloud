package com.testwa.distest.client.boost.impl;

import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;

/**
 * Created by wen on 16/8/14.
 */
//@Component
public class TestwaNotificationCallbackImpl implements TestwaNotificationCallback{
    @Override
    public void done(Object o, TestwaException e) throws TestwaException {
        System.out.println("TestwaNotificationCallbackImpl");

    }
}
