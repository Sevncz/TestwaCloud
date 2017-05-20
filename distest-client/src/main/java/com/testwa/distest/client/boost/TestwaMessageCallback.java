package com.testwa.distest.client.boost;

/**
 * Created by wen on 16/8/14.
 */
public interface TestwaMessageCallback extends TestwaCallback<Object, TestwaException> {
    @Override
    void done(Object o, TestwaException e) throws TestwaException;
}
