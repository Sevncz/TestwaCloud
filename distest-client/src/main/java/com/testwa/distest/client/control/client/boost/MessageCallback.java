package com.testwa.distest.client.control.client.boost;

/**
 * Created by wen on 16/8/14.
 */
public interface MessageCallback extends Callback<Object, MessageException> {
    @Override
    void done(Object o, MessageException e) throws MessageException;
}
