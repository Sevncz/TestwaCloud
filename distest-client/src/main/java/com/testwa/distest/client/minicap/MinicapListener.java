package com.testwa.distest.client.minicap;

/**
 * Created by wen on 2017/4/18.
 */
public interface MinicapListener {
    // minicap启动完毕后
    public void onStartup(Minicap minicap, boolean success);
    // minicap关闭
    public void onClose(Minicap minicap);
    // banner信息读取完毕
    public void onBanner(Minicap minicap, Banner banner);
    // 读取到图片信息
    public void onJPG(Minicap minicap, byte[] data);
}
