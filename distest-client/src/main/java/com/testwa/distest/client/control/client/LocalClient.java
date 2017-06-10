package com.testwa.distest.client.control.client;

import com.sun.scenario.effect.ImageData;
import com.testwa.distest.client.control.Protocol;
import com.testwa.distest.client.minicap.Banner;
import com.testwa.distest.client.minicap.Minicap;
import com.testwa.distest.client.minicap.MinicapListener;
import com.testwa.distest.client.minitouch.Minitouch;
import com.testwa.distest.client.minitouch.MinitouchListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wen on 03/06/2017.
 */
public class LocalClient extends BaseClient implements MinicapListener, MinitouchListener {
    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<ImageData> dataQueue = new LinkedBlockingQueue<ImageData>();

    private Protocol protocol;

    public LocalClient(Protocol protocol) {
        this.protocol = protocol;
    }


    public static class ImageData {
        ImageData(byte[] d) {
            timesp = System.currentTimeMillis();
            data = d;
        }
        long timesp;
        byte[] data;
    }


    @Override
    public void onStartup(Minicap minicap, boolean success) {

    }

    @Override
    public void onClose(Minicap minicap) {

    }

    @Override
    public void onBanner(Minicap minicap, Banner banner) {

    }

    @Override
    public void onJPG(Minicap minicap, byte[] data) {

    }

    @Override
    public void onStartup(Minitouch minitouch, boolean success) {

    }

    @Override
    public void onClose(Minitouch minitouch) {

    }


}
