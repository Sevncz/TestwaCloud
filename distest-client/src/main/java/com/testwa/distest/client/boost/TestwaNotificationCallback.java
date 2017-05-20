package com.testwa.distest.client.boost;

import org.springframework.stereotype.Service;

/**
 * Created by wen on 16/8/14.
 */
@Service
public interface TestwaNotificationCallback extends TestwaCallback<Object, TestwaException> {
    @Override
    void done(Object o, TestwaException e) throws TestwaException;
}
