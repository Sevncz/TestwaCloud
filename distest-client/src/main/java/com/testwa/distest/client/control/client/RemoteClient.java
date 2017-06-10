package com.testwa.distest.client.control.client;

import com.alibaba.fastjson.JSONObject;
import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.core.Command;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.control.message.BinaryMessage;
import com.testwa.distest.client.control.message.FileMessage;
import com.testwa.distest.client.minicap.Banner;
import com.testwa.distest.client.minicap.Minicap;
import com.testwa.distest.client.minicap.MinicapListener;
import com.testwa.distest.client.minitouch.Minitouch;
import com.testwa.distest.client.minitouch.MinitouchListener;
import com.testwa.distest.client.util.Constant;
import com.testwa.distest.client.web.startup.MainClientConnect;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wen on 10/06/2017.
 */
public class RemoteClient extends BaseClient implements MinicapListener, MinitouchListener {
    private static Logger log = LoggerFactory.getLogger(RemoteClient.class);

    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<LocalClient.ImageData> dataQueue = new LinkedBlockingQueue<LocalClient.ImageData>();

    private String url;
    private String controller;
    private String serialNumber;
    private Socket ws;

    Minicap minicap = null;
    Minitouch minitouch = null;

    public RemoteClient(String url,String controller, String serialNumber) throws IOException, URISyntaxException {
        this.url = url;
        this.controller = controller;
        this.serialNumber = serialNumber;
        if (serialNumber == null || serialNumber.isEmpty()) {
            AndroidDevice device = AndroidHelper.getInstance().getFirstDevice();
            if (device == null)
                throw new RuntimeException("未找到设备！");
            this.serialNumber = device.getSerialNumber();
        }

        ws = IO.socket(url);
        ws.on(Socket.EVENT_CONNECT, args -> {
            JSONObject obj = new JSONObject();
            obj.put("sn", serialNumber);
            log.info("websocket connect %s", this.serialNumber);
        }).on(Socket.EVENT_DISCONNECT, args -> {
            log.info("websocket disconnect %s", this.serialNumber);
        });
        ws.connect();
    }

    private void startMinicap(Command command) {
        if (minicap != null) {
            minicap.kill();
        }
        // 获取请求的配置
        JSONObject obj = (JSONObject) command.get("config");
        Float scale = obj.getFloat("scale");
        Float rotate = obj.getFloat("rotate");
        if (scale == null) {scale = 0.3f;}
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        if (rotate == null) { rotate = 0.0f; }
        Minicap minicap = new Minicap(serialNumber);
        minicap.addEventListener(this);
        minicap.start(scale, rotate.intValue());
        this.minicap = minicap;
    }

    private void startMinitouch() {
        if (minitouch != null) {
            minicap.kill();
        }

        Minitouch minitouch = new Minitouch(serialNumber);
        minitouch.addEventListener(this);
        minitouch.start();
        this.minitouch = minitouch;
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
