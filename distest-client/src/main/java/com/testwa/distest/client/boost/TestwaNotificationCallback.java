package com.testwa.distest.client.boost;

import com.testwa.distest.client.control.client.boost.Callback;
import com.testwa.distest.client.control.client.boost.MessageException;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 16/8/14.
 */
@Service
public interface TestwaNotificationCallback extends Callback<Object, TestwaException> {
    @Override
    void done(Object o, TestwaException e) throws MessageException;
}
