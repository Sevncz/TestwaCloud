package com.testwa.distest.client.control.boost;

/**
 * Created by wen on 16/8/14.
 */
public interface Callback<X, T extends Throwable> {

    public void done(X x,T t) throws MessageException;

}
