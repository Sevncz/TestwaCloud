package com.testwa.distest.client.control.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.google.protobuf.ByteString;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.common.enums.Command;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.executor.*;
import com.testwa.distest.client.component.logcat.Logcat;
import com.testwa.distest.client.component.logcat.LogcatListener;
import com.testwa.distest.client.component.stfservice.KeyCode;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.component.minicap.Banner;
import com.testwa.distest.client.component.minicap.Minicap;
import com.testwa.distest.client.component.minicap.MinicapListener;
import com.testwa.distest.client.component.minitouch.Minitouch;
import com.testwa.distest.client.component.minitouch.MinitouchListener;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.util.Http;
import io.grpc.Channel;
import io.rpc.testwa.device.LogcatRequest;
import io.rpc.testwa.device.ScreenCaptureRequest;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.openqa.selenium.os.CommandLine;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.cvMat;
import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvDecodeImage;

/**
 * Created by wen on 10/06/2017.
 */
@Slf4j
public class RemoteClient extends BaseClient implements MinicapListener, MinitouchListener, TaskListener, LogcatListener {

    static final int DATA_TIMEOUT = 100; //ms
    private boolean isWaitting = false;
    private boolean isLogcatWaitting = false;
    private BlockingQueue<LocalClient.ImageData> dataQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<LocalClient.ImageData> toVideoDataQueue = new LinkedBlockingQueue<>();

    private String serialNumber;
    private Socket ws;
    private Channel channel;

    private String resourcesPath;  // minicap和minitouch存放的路径

    private Minicap minicap = null;
    private Minitouch minitouch = null;
    private Task task = null;
    private Logcat logcat = null;

    private boolean isVideo;

    public RemoteClient(String url, String serialNumber, Channel channel, String resourcesPath) throws URISyntaxException {
        log.info("Remote Client init");
        this.serialNumber = serialNumber;

        this.resourcesPath = resourcesPath;

        this.channel = channel;

        ws = IO.socket(url);
        ws.on(Socket.EVENT_CONNECT, args -> {
            JSONObject obj = new JSONObject();
            obj.put("sn", serialNumber);
            obj.put("key", "");
            log.info("设备{}已连接.", serialNumber);
            ws.emit(Command.Schem.OPEN.getSchemString(), obj.toJSONString());
        }).on(Socket.EVENT_DISCONNECT, args -> {
            log.info("设备{}断开连接.", this.serialNumber);
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
            AndroidHelper.getInstance().unInstallApp(keyboardPath, serialNumber);
            AndroidHelper.getInstance().installApp(keyboardPath, serialNumber);
        }catch (Exception e){
            log.error("安装中文输入失败, {}", keyboardPath);
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
            case START_LOGCAT:
                logcatCommand(command);
                break;
            case WAIT_LOGCAT:
                waitlogcatCommand(command);
                break;
            case START_TASK:
                startTask(command);
                break;
            case CANCEL_TASK:
                cancelTask(command);
                break;
            case INSTALL:
                installApp(command);
                break;
            case UNINSTALL:
                uninstallApp(command);
                break;
            case SHELL:
                shellCommand(command);
                break;
            case OPENWEB:
                openWebCommand(command);
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

    // minicap cmd
    private Thread tovideoThread;
    private LocalClient.ImageData imageData = null;
    private boolean push = true;

    private void startMinicap(Command command) {
        log.info("startMinicap {}", command.getCommandString());
        if (minicap != null) {
            minicap.kill();
        }
        // 获取请求的配置
        JSONObject obj = (JSONObject) command.get("config");
        Float scale = obj.getFloat("scale");
        Float rotate = obj.getFloat("rotate");
        scale = 0.5f;
        if (scale == null) {scale = 0.3f;}
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        if (rotate == null) { rotate = 0.0f; }
        Minicap minicap = new Minicap(serialNumber, resourcesPath);
        minicap.addEventListener(this);
        minicap.start(scale, rotate.intValue());
        this.minicap = minicap;
    }

    @Override
    public void onStartup(Minicap minicap, boolean success) {
        if (ws != null) {
            ws.emit(Command.Schem.MINICAP.getSchemString(), "open");
        }
//        tovideoThread = new Thread(new ConvertRunner());
//        tovideoThread.setDaemon(false);
//        tovideoThread.start();
    }

    @Override
    public void onClose(Minicap minicap) {
        push = false;
        if (ws != null) {
            ws.emit(Command.Schem.MINICAP.getSchemString(), "close");
        }
    }

    @Override
    public void onBanner(Minicap minicap, Banner banner) {

    }

    @Override
    public void onJPG(Minicap minicap, byte[] data) {
        // for video queue
        toVideoDataQueue.add(new LocalClient.ImageData(data));
        // for pic queue
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

    class ConvertRunner implements Runnable {
        private FFmpegFrameRecorder recorder;
        private LocalClient.ImageData lastD = null;
        private FileOutputStream out = null;
        private int FRAME_RATE = 30;

        public ConvertRunner(){

            int h = (int) (minicap.getDeviceSize().h * 0.3);
            int w = (int) (minicap.getDeviceSize().w * 0.3);
            int m_dst_h = (h >> 4) << 4 ;
            int m_dst_w = (w >> 4) << 4 ;

            String r1 = "rtmp://122.115.49.75:8090/live/h265";
            String r2 = "rtmp://cloud.testwa.com:1935/live/h265";
            String r3 = Constant.localVideoPath + File.separator + "testwa-recorder.flv";
            log.info(r3);
            recorder = new FFmpegFrameRecorder(r3, m_dst_w, m_dst_h, 2);
            recorder.setInterleaved(true);
            /**
             * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
             * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
             * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
             */

            recorder.setVideoOption("tune", "zerolatency");
            /**
             * 权衡quality(视频质量)和encode speed(编码速度) values(值)：
             * ultrafast(终极快),superfast(超级快), veryfast(非常快), faster(很快), fast(快),
             * medium(中等), slow(慢), slower(很慢), veryslow(非常慢)
             * ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；而veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
             * 参考：https://trac.ffmpeg.org/wiki/Encode/H.264 官方原文参考：-preset ultrafast
             * as the name implies provides for the fastest possible encoding. If
             * some tradeoff between quality and encode speed, go for the speed.
             * This might be needed if you are going to be transcoding multiple
             * streams on one machine.
             */
            recorder.setVideoOption("preset", "ultrafast");
            /**
             * 参考转流命令: ffmpeg
             * -i'udp://localhost:5000?fifo_size=1000000&overrun_nonfatal=1' -crf 30
             * -preset ultrafast -acodec aac -strict experimental -ar 44100 -ac
             * 2-b:a 96k -vcodec libx264 -r 25 -b:v 500k -f flv 'rtmp://<wowza
             * serverIP>/live/cam0' -crf 30
             * -设置内容速率因子,这是一个x264的动态比特率参数，它能够在复杂场景下(使用不同比特率，即可变比特率)保持视频质量；
             * 可以设置更低的质量(quality)和比特率(bit rate),参考Encode/H.264 -preset ultrafast
             * -参考上面preset参数，与视频压缩率(视频大小)和速度有关,需要根据情况平衡两大点：压缩率(视频大小)，编/解码速度 -acodec
             * aac -设置音频编/解码器 (内部AAC编码) -strict experimental
             * -允许使用一些实验的编解码器(比如上面的内部AAC属于实验编解码器) -ar 44100 设置音频采样率(audio sample
             * rate) -ac 2 指定双通道音频(即立体声) -b:a 96k 设置音频比特率(bit rate) -vcodec libx264
             * 设置视频编解码器(codec) -r 25 -设置帧率(frame rate) -b:v 500k -设置视频比特率(bit
             * rate),比特率越高视频越清晰,视频体积也会变大,需要根据实际选择合理范围 -f flv
             * -提供输出流封装格式(rtmp协议只支持flv封装格式) 'rtmp://<FMS server
             * IP>/live/cam0'-流媒体服务器地址
             */
            recorder.setVideoOption("crf","28");
            recorder.setVideoBitrate(2000000);
            // 编/解码器
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            // 封装格式
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFormat("flv");
            // mkv
//                recorder.setFormat("mastorka");
//                recorder.setVideoQuality(0); // lossless
            // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
            recorder.setFrameRate(FRAME_RATE);
            // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
            recorder.setGopSize(FRAME_RATE * 2);

            try {
                recorder.start();
            } catch (FrameRecorder.Exception e) {
                if (recorder != null) {
                    log.error("recorder start error!", e);
                    try {
                        recorder.stop();
                        recorder.start();
                    } catch (FrameRecorder.Exception e1) {
                        try {
                            log.error("recorder start error again!", e);
                            recorder.stop();
                        } catch (FrameRecorder.Exception e2) {
                            log.error("recorder start error again again!", e);
                        }
                    }
                }
            }

        }

        @Override
        public void run() {
            while(push){
                LocalClient.ImageData d1 = null;
                try {
                    d1 = toVideoDataQueue.poll(DATA_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (d1 == null) {
                    if(lastD != null){
                        toVideo(lastD.data);
                    }
                }else{
                    lastD = d1;
                    toVideo(d1.data);
                }
            }
            // 关闭录制
            try {
                recorder.stop();
                if(out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        log.error("out close error", e);
                    }
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
        private void toVideo(byte[] data){
            if(data.length == 0){
                return;
            }
            OpenCVFrameConverter.ToIplImage conveter = new OpenCVFrameConverter.ToIplImage();
            opencv_core.IplImage image = cvDecodeImage(cvMat(1, data.length, CV_8UC1, new BytePointer(data)));
            try {
                recorder.record(conveter.convert(image));
                cvReleaseImage(image);
            } catch (Exception e) {
                log.error("abort", e);
            }

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
        // 关闭组件
        if (minitouch != null) {
            minitouch.kill();
        }
        if (minicap != null) {
            minicap.kill();
        }
        if (task != null) {
            task.kill();
        }
        if(ws != null){
            this.ws.disconnect();
            this.channel = null;
        }
        // 关闭录制
        push = false;
    }

    // logcat

    /**
     * 等待时，关闭logcat
     * @param command
     */
    private void waitlogcatCommand(Command command) {
        log.info("receive waite cmd");
        if(this.logcat != null){
            this.logcat.close();
        }
    }

    /**
     * 再次发送命令下来时，重新打开logcat
     * @param command
     */
    private void logcatCommand(Command command) {
        log.info("logcat start");
        if(this.logcat != null){
            this.logcat.close();
        }
        this.isLogcatWaitting = true;
        String content = (String) command.get("content");
        Logcat l = new Logcat(this.serialNumber, content);
        l.addEventListener(this);
        l.start();
        this.logcat = l;
    }

    @Override
    public void onStartup(Logcat logcat, boolean success) {
        if (ws != null) {
            ws.emit(Command.Schem.START_LOGCAT.getSchemString(), success);
        }
    }

    @Override
    public void onClose(Logcat logcat) {
        if (ws != null) {
            ws.emit(Command.Schem.WAIT_LOGCAT.getSchemString());
        }

    }

    @Override
    public void onLog(Logcat logcat, byte[] data) {
        if (isLogcatWaitting) {
            sendLog(data);
        }
    }

    private void sendLog(byte[] data) {
        log.debug(String.valueOf(data.length));
        try {

            LogcatRequest request = LogcatRequest.newBuilder()
                    .setSerial(this.serialNumber)
                    .setContent(ByteString.copyFrom(data))
                    .build();

            Gvice.deviceService(this.channel).logcat(request);

        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
        }
    }

    // task
    @Override
    public void onStartup(Task task, boolean success) {
        if (ws != null) {
            ws.emit(Command.Schem.START_TASK.getSchemString(), success);
        }
    }

    @Override
    public void onComplete(Task task) {
        if (ws != null) {
            ws.emit(Command.Schem.COMPLETE_TASK.getSchemString(), true);
        }
    }

    @Override
    public void onCancel(Task task) {
        if (ws != null) {
            ws.emit(Command.Schem.CANCEL_TASK.getSchemString(), true);
        }
    }

    /**
     *@Description: 开始执行测试任务
     *@Param: [command]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    private void startTask(Command command) {
        log.info("start PythonExecutor {}", command.getCommandString());
        if (task != null) {
            task.kill();
        }
        String cmdJson = command.getContent();
        RemoteRunCommand cmd = JSON.parseObject(cmdJson, RemoteRunCommand.class);
        Task task = new Task(cmd);
        task.addEventListener(this);
        task.start();
        this.task = task;
    }

    /**
     *@Description: 取消在该设备正在执行的任务
     *@Param: [command]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    private void cancelTask(Command command) {
        task.kill();
    }

    /**
     *@Description: 安装app
     *@Param: [command]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    private void installApp(Command command) {
        String cmdJson = command.getContent();
        Thread t = new Thread(new InstallRunner(cmdJson));
        t.start();

    }

    class InstallRunner implements Runnable{
        private String cmdJson;

        public InstallRunner(String cmdJson) {
            this.cmdJson = cmdJson;
        }

        @Override
        public void run() {
            AppInfo appInfo = JSON.parseObject(cmdJson, AppInfo.class);

            AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(serialNumber);
            String distestApiWeb = Config.getString("distest.api.web");

            String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());
            String appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileName();

            // 检查是否有和该app md5一致的
            try {
                new Downloader(appUrl, appLocalPath);
            } catch (DownloadFailException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DefaultAndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));
            device.install(androidApp);
        }
    }

    /**
     *@Description: 卸载App
     *@Param: [command]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    private void uninstallApp(Command command) {
        String appBasePackage = (String) command.get("appBasePackage");
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(serialNumber);
        device.uninstall(appBasePackage);
    }

    /**
     *@Description: 执行并返回shell命令的结果
     *@Param: [command]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/10
     */
    private void shellCommand(Command command) {
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(serialNumber);
        String result = device.runAdbCommand("shell " + command.getContent());
        log.info(result);
    }

    private void openWebCommand(Command command) {
        String cmd = "shell am start -a android.intent.action.VIEW -d ";
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(serialNumber);

        String result = device.runAdbCommand(cmd + command.getContent());
        log.info(result);
    }

}
