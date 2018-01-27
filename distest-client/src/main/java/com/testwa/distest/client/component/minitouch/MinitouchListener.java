package com.testwa.distest.client.component.minitouch;


/**
 * Created by wen on 2017/4/19.
 */
public interface MinitouchListener {
    // minitouch启动完毕后
    void onStartup(Minitouch minitouch, boolean success);
    // minitouch关闭后
    void onClose(Minitouch minitouch);
}
