package com.testwa.distest.client.boost;

/**
 * Created by wen on 16/8/14.
 */
public interface TestwaCallback<X, T extends Throwable> {

    public void done(X x,T t) throws TestwaException;

}
