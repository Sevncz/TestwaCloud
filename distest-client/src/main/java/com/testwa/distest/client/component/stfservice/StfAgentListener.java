package com.testwa.distest.client.component.stfservice;/** * Created by wen on 2017/4/19. */public interface StfAgentListener {    // StfAgent启动完毕后    void onStartup(StfAgent stfAgent, boolean success);    // StfAgent关闭后    void onClose(StfAgent stfAgent);}