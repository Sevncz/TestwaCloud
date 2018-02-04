package com.testwa.distest.client.control.client;

import com.alibaba.fastjson.JSONObject;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.google.protobuf.ByteString;
import com.testwa.core.common.enums.Command;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.component.stfservice.KeyCode;
import com.testwa.distest.client.component.stfservice.StfAgent;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.component.minicap.Banner;
import com.testwa.distest.client.component.minicap.Minicap;
import com.testwa.distest.client.component.minicap.MinicapListener;
import com.testwa.distest.client.component.minitouch.Minitouch;
import com.testwa.distest.client.component.minitouch.MinitouchListener;
import com.testwa.distest.client.component.Constant;
import io.grpc.Channel;
import io.rpc.testwa.device.ScreenCaptureRequest;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wen on 10/06/2017.
 */
@Slf4j
public class RemoteClient extends BaseClient implements MinicapListener, MinitouchListener {

    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private BlockingQueue<LocalClient.ImageData> dataQueue = new LinkedBlockingQueue<LocalClient.ImageData>();

    private String url;
    private String controller;
    private String serialNumber;
    private Socket ws;
    private Channel channel;

    private String resourcesPath;  // minicap和minitouch存放的路径

    Minicap minicap = null;
    Minitouch minitouch = null;
    StfAgent stfAgent = null;

    public RemoteClient(String url, String controller, String serialNumber, Channel channel, String resourcesPath) throws IOException, URISyntaxException {
        log.info("Remote Client init");
        this.url = url;
        this.controller = controller;
        this.serialNumber = serialNumber;

        this.resourcesPath = resourcesPath;

        this.channel = channel;

        ws = IO.socket(url);
        ws.on(Socket.EVENT_CONNECT, args -> {
            JSONObject obj = new JSONObject();
            obj.put("sn", serialNumber);
            obj.put("key", "");
            log.info("Remote client {} connected.", serialNumber);
            ws.emit(Command.Schem.OPEN.getSchemString(), obj.toJSONString());
        }).on(Socket.EVENT_DISCONNECT, args -> {
            log.info("Remote client {} disconnect.", this.serialNumber);
        });

        for(Command.Schem schem : Command.Schem.values()){
            ws.on(schem.getSchemString(), args -> {
                if (args.length == 1){
                    Command command = Command.ParseCommand(schem.getSchemString(), (String)args[0]);
                    executeCommand(command);
                }
            });
        }

        ws.connect();

        // install 支持中文输入的输入法
        String keyboardPath = this.resourcesPath + File.separator + Constant.getKeyboardService();
        try {
            AndroidHelper.getInstance().installApp(keyboardPath, serialNumber);
        }catch (Exception e){
            log.error("install keyboard service error, {}", keyboardPath);
        }
    }

    @Override
    public void onStartup(Minicap minicap, boolean success) {
        if (ws != null) {
            ws.emit(Command.Schem.MINICAP.getSchemString(), "open");
        }
    }

    @Override
    public void onClose(Minicap minicap) {
        if (ws != null) {
            ws.emit(Command.Schem.MINICAP.getSchemString(), "close");
        }
    }

    @Override
    public void onBanner(Minicap minicap, Banner banner) {

    }

    @Override
    public void onJPG(Minicap minicap, byte[] data) {
        if (isWaitting) {
            if (dataQueue.size() > 0) {
                dataQueue.add(new LocalClient.ImageData(data));
                // 挑选没有超时的图片
                LocalClient.ImageData d = getUsefulImage();
                sendImage(d.data);
            } else {
                sendImage(data);
            }
//            isWaitting = false;
        } else {
            clearObsoleteImage();
            dataQueue.add(new LocalClient.ImageData(data));
        }
    }

    @Override
    public void onStartup(Minitouch minitouch, boolean success) {
        if (ws != null) {
            ws.emit(Command.Schem.MINITOUCH.getSchemString(), "open");
        }
    }

    @Override
    public void onClose(Minitouch minitouch) {
        if (ws != null) {
            ws.emit(Command.Schem.MINITOUCH.getSchemString(), "close");
        }

    }

    private void sendImage(byte[] data) {
        log.debug(String.valueOf(data.length));
        try {

            ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()
                    .setImg(ByteString.copyFrom(data))
                    .setName("xxx")
                    .setSerial(this.serialNumber)
                    .build();

            Gvice.deviceService(this.channel).screen(request);

        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
        }
    }

    private void clearObsoleteImage() {
        LocalClient.ImageData d = dataQueue.peek();
        long curTS = System.currentTimeMillis();
        while (d != null) {
            if (curTS - d.timesp < DATA_TIMEOUT) {
                dataQueue.poll();
                d = dataQueue.peek();
            } else {
                break;
            }
        }
    }

    private LocalClient.ImageData getUsefulImage() {
        long curTS = System.currentTimeMillis();
        // 挑选没有超时的图片
        LocalClient.ImageData d = null;
        while (true) {
            d = dataQueue.poll();
            // 如果没有超时，或者超时了但是最后一张图片，也发送给客户端
            if (d == null || curTS - d.timesp < DATA_TIMEOUT || dataQueue.size() == 0) {
                break;
            }
        }
        return d;
    }

    public void setWaitting(boolean waitting) {
        isWaitting = waitting;
        trySendImage();
    }

    private void trySendImage() {
        LocalClient.ImageData d = getUsefulImage();
        if (d != null) {
            isWaitting = false;
            sendImage(d.data);
        }
    }

    void executeCommand(Command command) {
        switch (command.getSchem()) {
            case START:
                startCommand(command);
                break;
            case TOUCH:
                touchCommand(command);
            case WAITTING:
                waittingCommand(command);
                break;
            case WAIT:
                waitCommand(command);
                break;
            case KEYEVENT:
                keyeventCommand(command);
                break;
            case INPUT:
                inputCommand(command);
                break;
            case PUSH:
                pushCommand(command);
                break;
            case BACK:
                backCommand(command);
                break;
            case HOME:
                homeCommand(command);
                break;
            case MENU:
                menuCommand(command);
                break;
        }
    }

    private void startCommand(Command command) {
        log.info("startCommand {}", command.getCommandString());
        String str = command.getString("type", null);
        if (str != null) {
            if (str.equals("minicap")) {
                startMinicap(command);
            } else if (str.equals("minitouch")) {
                startMinitouch(command);
            }else if (str.equals("stfagent")) {
//                startStfAgent(command);
            }
        }
    }

    private void waittingCommand(Command command) {
        setWaitting(true);
    }

    private void waitCommand(Command command) {
        setWaitting(false);
    }

    // minitouch cmd
    private void keyeventCommand(Command command) {
        int k = Integer.parseInt(command.getContent());
        if (minitouch != null) minitouch.sendKeyEvent(k);
    }


    private void touchCommand( Command command) {
        String str = (String) command.getContent();
        if (minitouch != null) minitouch.sendEvent(str);
    }

    private void inputCommand(Command command) {
        String str = (String) command.getContent();
        if (minitouch != null) minitouch.inputText(str);
    }

    private void pushCommand(Command command) {
        String name = command.getString("name", null);
        String path = command.getString("path", null);

        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(serialNumber);
        try {
            device.getDevice().pushFile(Constant.getTmpFile(name).getAbsolutePath(), path + "/" + name);
        } catch (Exception e) {
        }
    }
    // stfagent cmd
    private void backCommand(Command command){
        try {
            CommandLine commandLine = new CommandLine(AndroidSdk.adb().getCanonicalPath(),
                    "-s",
                    serialNumber,
                    "shell",
                    "input",
                    "keyevent",
                    KeyCode.KEYCODE_BACK+""
            );
            commandLine.executeAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void menuCommand(Command command){
        try {
            CommandLine commandLine = new CommandLine(AndroidSdk.adb().getCanonicalPath(),
                    "-s",
                    serialNumber,
                    "shell",
                    "input",
                    "keyevent",
                    KeyCode.KEYCODE_MENU+""
            );
            commandLine.executeAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void homeCommand(Command command){
        try {
            CommandLine commandLine = new CommandLine(AndroidSdk.adb().getCanonicalPath(),
                    "-s",
                    serialNumber,
                    "shell",
                    "input",
                    "keyevent",
                    KeyCode.KEYCODE_HOME+""
            );
            commandLine.executeAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void installCommand(Command command){

    }

    private void startMinicap(Command command) {
        log.info("startMinicap {}", command.getCommandString());
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
        Minicap minicap = new Minicap(serialNumber, resourcesPath);
        minicap.addEventListener(this);
        minicap.start(scale, rotate.intValue());
        this.minicap = minicap;
    }

    private void startMinitouch(Command command) {
        log.info("start Minitouch {}", command.getCommandString());
        if (minitouch != null) {
            minitouch.kill();
        }

        Minitouch minitouch = new Minitouch(serialNumber, resourcesPath);
        minitouch.addEventListener(this);
        minitouch.start();
        this.minitouch = minitouch;
    }

//    private void startStfAgent(Command command) {
//        log.info("start stfagent {}", command.getCommandString());
//        if (stfAgent != null) {
//            stfAgent.kill();
//        }
//        StfAgent stfAgent = new StfAgent(serialNumber, resourcesPath);
//        stfAgent.addEventListener(this);
//        stfAgent.start();
//        this.stfAgent = stfAgent;
//    }

    public void stop(){
        if (minitouch != null) {
            minitouch.kill();
        }
        if (minicap != null) {
            minicap.kill();
        }
        if(ws != null){
            this.ws.disconnect();
            this.channel = null;
        }
    }

}
