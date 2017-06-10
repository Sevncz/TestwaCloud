package com.testwa.distest.client.minitouch;


/**
 * Created by harry on 2017/4/19.
 */
public interface MinitouchListener {
    // minitouch启动完毕后
    void onStartup(Minitouch minitouch, boolean success);
    // minitouch关闭后
    void onClose(Minitouch minitouch);
}
