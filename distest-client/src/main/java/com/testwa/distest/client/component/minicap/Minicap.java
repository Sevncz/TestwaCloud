package com.testwa.distest.client.component.minicap;

import com.android.ddmlib.*;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.MinicapServiceBuilder;
import com.testwa.core.utils.Identities;
import com.testwa.distest.client.android.AdbForward;
import com.testwa.core.utils.Common;

import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.control.port.ScreenPortProvider;
import com.testwa.distest.client.component.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class Minicap {
    private BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

    private Banner banner = new Banner();
    private Socket minicapSocket;
    private IDevice device;
    private String resourcesPath;
    // 物理屏幕宽高
    private Size deviceSize;
    private AdbForward forward;
    private boolean isRunning = false;

    private static String BIN = "";
    private static String MINICAP_CHMOD_COMMAND = "chmod 777 %s/%s";
    private static String MINICAP_WM_SIZE_COMMAND = "wm size";
    private static String MINICAP_DIR_COMMAND = String.format("mkdir %s 2>/dev/null || true", Constant.MINICAP_DIR);
//    private static String MINICAP_TAKESCREENSHOT_COMMAND = "LD_LIBRARY_PATH=%s %s/%s -P %dx%d@%dx%d/0 -s > %s";

    private AdbDriverService service;
    // 启动minicap的线程
    private Thread minicapInitialThread, dataReaderThread, imageParserThread;

    // listener
    private List<MinicapListener> listenerList = new ArrayList<MinicapListener>();

    public Minicap(String serialNumber, String resourcesPath) {

        this.resourcesPath = resourcesPath;
        int install = 5;
        while (install > 0) {
            try {
                this.device = AndroidHelper.getInstance().getAndroidDevice(serialNumber).getDevice();
                installMinicap(device, resourcesPath);
                Thread.sleep(1000);
                break;
            } catch (MinicapInstallException | DeviceNotFoundException e) {
                install--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 获取设备屏幕的尺寸
        String output = AndroidHelper.getInstance().executeShellCommand(device, MINICAP_WM_SIZE_COMMAND);
        if (output != null && !output.isEmpty()) {
            String sizeStr = output.split(":")[1].trim();
            int screenWidth = Integer.parseInt(sizeStr.split("x")[0].trim());
            int screenHeight = Integer.parseInt(sizeStr.split("x")[1].trim());
            deviceSize = new Size(screenWidth, screenHeight);
        }
    }

    //判断是否支持minicap
    public boolean isSupoort() {
        String supportCommand = String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -t", deviceSize.w, deviceSize.h);
        String output = AndroidHelper.getInstance().executeShellCommand(device, supportCommand);
        if (output.trim().endsWith("OK")) {
            return true;
        }
        return false;
    }


    public void addEventListener(MinicapListener listener) {
        if (listener != null) {
            this.listenerList.add(listener);
        }
    }

    /**
     * 将minicap的二进制和.so文件push到/data/local/tmp文件夹下，启动minicap服务
     */
    private static void installMinicap(IDevice device, String resourcesPath) throws MinicapInstallException {

        String abi = device.getProperty(Constant.PROP_ABI);
        String sdk = device.getProperty(Constant.PROP_SDK);
        String rel = device.getProperty(Constant.PROP_REL);


        if (StringUtils.isEmpty(sdk) || StringUtils.isEmpty(abi)) {
            throw new MinicapInstallException("cant not get device info. please check device is connected");
        }

        Integer sdkvalue = Integer.parseInt(sdk);
        if (sdkvalue >= 16) {
            BIN = Constant.MINICAP_BIN;
        } else {
            BIN = Constant.MINICAP_NOPIE;
        }

        String minicapBinPath = resourcesPath + File.separator + Constant.getMinicapBin() + File.separator + abi + File.separator + BIN;
        String minicapSoPath = resourcesPath + File.separator + Constant.getMinicapSo() + File.separator + "android-" + sdk + File.separator + abi + File.separator + Constant.MINICAP_SO;

        // Create a directory for minicap resources
        AndroidHelper.getInstance().executeShellCommand(device, MINICAP_DIR_COMMAND);

        try {
            // 将minicap的可执行文件和.so文件一起push到设备中
            String minicapCheckOut = AndroidHelper.getInstance().executeShellCommand(device, String.format("ls %s | grep %s", Constant.MINICAP_DIR, BIN));
            // Upload the binary
            if (!minicapCheckOut.contains(BIN)) {
                device.pushFile(minicapBinPath, Constant.MINICAP_DIR + "/" + BIN);
            }
            // Upload the shared library
            if (!minicapCheckOut.contains(Constant.MINICAP_SO)) {
                device.pushFile(minicapSoPath, Constant.MINICAP_DIR + "/" + Constant.MINICAP_SO);
            }
            AndroidHelper.getInstance().executeShellCommand(device, String.format(MINICAP_CHMOD_COMMAND, Constant.MINICAP_DIR, BIN));
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }
    }


    public AdbForward createForward() {
        forward = generateForwardInfo();
        int tryTime = 10;
        while(tryTime >= 0){
            try {
                device.createForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
                return forward;
            } catch (Exception e) {
                log.error("create forward failed", e);
                tryTime--;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    private void removeForward(AdbForward forward) {
        if (forward == null || !forward.isForward()) {
            return;
        }
        try {
            device.removeForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (AdbCommandRejectedException e) {
            log.info("removeForward: AdbCommandRejectedException, {}", e.getMessage());
        } catch (IOException e) {
            log.error("removeForward: IOException, {}", e.getMessage());
        } catch (TimeoutException e) {
            log.error("removeForward: TimeoutException, {}", e.getMessage());
        }
    }

    /**
     * 生成forward信息
     */
    private AdbForward generateForwardInfo() {
        AdbForward[] forwards = AndroidHelper.getInstance().getForwardList();
        // serial_cap_number
        int maxNumber = 0;
        if (forwards.length > 0) {
            for (AdbForward forward : forwards) {
                if (forward.getSerialNumber().equals(device.getSerialNumber())) {
                    String l = forward.getLocalabstract();
                    String[] s = l.split("_");
                    if (s.length == 3) {
                        int n = Integer.parseInt(s[2]);
                        if (n > maxNumber) maxNumber = n;
                    }
                }
            }
        }
        maxNumber += 1;

        String forwardStr = String.format("%s_cap_%d", device.getSerialNumber(), maxNumber);
        int freePort = ScreenPortProvider.pullPort();
        AdbForward forward = new AdbForward(device.getSerialNumber(), freePort, forwardStr);
        return forward;
    }

    /*
    Usage: /data/local/tmp/minicap [-h] [-n <name>]
      -d <id>:       Display ID. (0)
      -n <name>:     Change the name of the abtract unix domain socket. (minicap)
      -P <value>:    Display projection (<w>x<h>@<w>x<h>/{0|90|180|270}).
      -Q <value>:    JPEG quality (0-100).
      -s:            Take a screenshot and output it to stdout. Needs -P.
      -S:            Skip frames when they cannot be consumed quickly enough.
      -t:            Attempt to get the capture method running, then exit.
      -i:            Get display information in JSON format. May segfault.
     */
    public String getMinicapCommand(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String name, String[] args) {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(String.format("LD_LIBRARY_PATH=%s", Constant.MINICAP_DIR));
        commands.add(Constant.MINICAP_DIR + "/" + BIN);
        commands.add("-P");
        commands.add(String.format("%dx%d@%dx%d/%d", ow, oh, dw, dh, rotate));
        commands.add("-n");
        commands.add(name);
        if (shipFrame)
            commands.add("-S");
        if (args != null) {
            for (String s : args) {
                commands.add(s);
            }
        }
        String command = StringUtils.join(commands, " ");
        log.info("minicap start command {}", command);
        return command;
    }

    /**
     * 屏幕截图
     * <p>
     * 由于个平台的换行符导致二进制流输出有问题，二进制数据将先base64编码后传输
     *
     * @return
     */
    public byte[] takeScreenShot() {

        String filename = Identities.randomLong() + ".jpg";
        String takeScreenShotCommand = getMinicapCommand(deviceSize.w, deviceSize.h, deviceSize.w, deviceSize.h, 0, false, filename, new String[]{"-s -b"});

        System.out.println("takeScreenShot:" + takeScreenShotCommand);
        BinaryOutputReceiver receiver = new BinaryOutputReceiver();
        try {
            device.executeShellCommand(takeScreenShotCommand, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // remove text output
        byte[] bytes = receiver.getOutput();
        do {
            String dataStr = new String(bytes);
            int jpgStart = dataStr.indexOf("/9j/");

            if (jpgStart >= 0) {
                dataStr = dataStr.substring(jpgStart); // jpg特征码base64
            } else {
                break;
            }

            try {
                bytes = new BASE64Decoder().decodeBuffer(dataStr);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (bytes[0] != -1 && bytes[1] != -40) {
                System.out.println("not a jpg file!!");
                break;
            }

            return bytes;
        } while (false);

        return new byte[0];
    }


    public void start(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String[] args) {

        if(StringUtils.isEmpty(BIN)){
            try {
                installMinicap(device, resourcesPath);
            } catch (MinicapInstallException e) {
                e.printStackTrace();
            }
        }

        isRunning = true;
        // 启动minicap服务
        AdbForward forward = createForward();

        if(forward == null){
            return;
        }
        service = new MinicapServiceBuilder()
                .whithBin(BIN)
                .whithDeviceId(device.getSerialNumber())
                .whithLibPath(Constant.MINICAP_DIR)
                .whithSize(String.format("%dx%d@%dx%d/%d", ow, oh, dw, dh, rotate))
                .whithRotate(rotate)
                .whithShipFrame(shipFrame)
                .whithName(forward.getLocalabstract())
                .whithArgs(args)
                .build();
        service.start();
        log.info("minicap vo start, forward port {}", forward.getPort());
        minicapInitialThread = new Thread(new StartInitial("127.0.0.1", forward.getPort()));
        minicapInitialThread.start();
    }

    public void start(final float scale, final int rotate) {
        /**
         * 这里有的时候会报null point异常，需要处理下
         */
        start(deviceSize.w, deviceSize.h, (int) (deviceSize.w * scale), (int) (deviceSize.h * scale), rotate, true, null);
    }

    public void reStart(final float scale, final int rotate) {
        isRunning = false;
        if (service != null) {
            service.stop();
        }

        if (dataReaderThread != null) {
            try {
                dataReaderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (imageParserThread != null) {
            try {
                imageParserThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        start(scale, rotate);
    }

    public void kill() {
        onClose();

        isRunning = false;
        if (service != null) {
            service.stop();
        }

        // 关闭socket
        if (minicapSocket != null && minicapSocket.isConnected()) {
            try {
                minicapSocket.close();
            } catch (IOException e) {
            }
            minicapSocket = null;
        }

        if (dataReaderThread != null) {
            try {
                dataReaderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (imageParserThread != null) {
            try {
                imageParserThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class StartInitial implements Runnable {
        private String host;
        private int port;

        public StartInitial(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void run() {
            log.info("StartInitial schedule start, host{}, port{}", this.host, this.port);
            try {
                byte[] bytes = null;
                int tryTime = 20;
                // 连接minicap启动的服务
                while (true) {
                    minicapSocket = new Socket(this.host, this.port);
                    InputStream stream = minicapSocket.getInputStream();
                    bytes = new byte[4096];

                    int n = stream.read(bytes);
                    if (n == -1) {
                        Thread.sleep(1000);
                        log.info("minicap socket close, retry again");
                        minicapSocket.close();
                    } else {

                        log.info("minicap ---------- start ");
                        // bytes内包含有信息，需要给Dataparser处理
                        dataQueue.add(Arrays.copyOfRange(bytes, 0, n));
                        isRunning = true;
                        onStartup(true);

                        // 启动 DataReader  ImageParser
                        dataReaderThread = startDataReaderThread(minicapSocket);
                        imageParserThread = startImageParserThread();
                        break;
                    }

                    tryTime--;
                    if (tryTime == 0) {
                        onStartup(false);
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("StartInitial error", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
            }

        }

    }

    private Thread startDataReaderThread(Socket minicapSocket) {
        Thread thread = new Thread(new DataReader(minicapSocket));
        thread.start();
        return thread;
    }

    private Thread startImageParserThread() {
        Thread thread = new Thread(new ImageParser());
        thread.start();
        return thread;
    }

    private void onStartup(boolean success) {
        for (MinicapListener listener : listenerList) {
            listener.onStartup(this, success);
        }
    }

    private void onClose() {
        for (MinicapListener listener : listenerList) {
            listener.onClose(this);
        }
        removeForward(forward);
    }

    private void onBanner(Banner banner) {
        for (MinicapListener listener : listenerList) {
            listener.onBanner(this, banner);
        }
    }

    private void onJPG(byte[] data) {
        for (MinicapListener listener : listenerList) {
            listener.onJPG(this, data);
        }
    }

    private class DataReader implements Runnable {
        static final int BUFF_SIZ = 4096;
        Socket socket = null;
        InputStream inputStream = null;
        long ts = 0;

        DataReader(Socket minicapSocket) {
            this.socket = minicapSocket;
            try {
                this.inputStream = minicapSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                onClose();
            }
        }

        @Override
        public void run() {
            try {
                readData();
            } catch (IOException e) {
                System.out.println("lost connection: " + e.getMessage());
                onClose();
            }
        }

        public void readData() throws IOException {
            log.debug("start read data");

            DataInputStream stream = new DataInputStream(inputStream);
            while (isRunning) {
                byte[] buffer = new byte[BUFF_SIZ];
                ts = System.currentTimeMillis();
                int len = stream.read(buffer);
                if (len == -1) {
                    return;
                }
                if (len == BUFF_SIZ) {
                    dataQueue.add(buffer);
                } else {
                    dataQueue.add(Common.subArray(buffer, 0, len));
                }
            }
        }

    }

    private class ImageParser implements Runnable {
        int readn = 0; // 已读大小
        int bannerLen = 2; // banner信息大小
        int readFrameBytes = 0;
        int frameBodyLength = 0;
        byte[] frameBody = new byte[0];
        long t = 0;

        @Override
        public void run() {
            while (isRunning) {
                try {
                    banner = new Banner();
                    readData();
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    onClose();
                }
            }
        }

        void readData() throws InterruptedException {
            byte[] buffer = dataQueue.poll(5000, TimeUnit.MILLISECONDS);
            if (buffer == null) { // TODO 使用阻塞队列就不用判断了
                return;
            }
            int length = buffer.length;
            for (int cursor = 0; cursor < length; ) {
                int ch = buffer[cursor] & 0xff;
                if (readn < bannerLen) {
                    cursor = parserBanner(cursor, ch);
                } else if (readFrameBytes < 4) { // frame length
                    frameBodyLength += (ch << (readFrameBytes * 8));
                    cursor += 1;
                    readFrameBytes += 1;
                    if (readFrameBytes == 4) {
                        t = System.currentTimeMillis();
                    }
                } else {
                    if (length - cursor >= frameBodyLength) {
                        byte[] subByte = Arrays.copyOfRange(buffer, cursor,
                                cursor + frameBodyLength);
                        frameBody = Common.mergeArray(frameBody, subByte);
                        if ((frameBody[0] != -1) || frameBody[1] != -40) {
                            log.error("Frame body does not start with JPG header");
                            return;
                        }
                        byte[] finalBytes = Arrays.copyOfRange(frameBody, 0, frameBody.length);
                        log.debug("to jpg");
                        onJPG(finalBytes);

                        cursor += frameBodyLength;
                        frameBodyLength = 0;
                        readFrameBytes = 0;
                        frameBody = new byte[0];

                        long timeused = (System.currentTimeMillis() - t);
                        timeused = timeused == 0 ? 1 : timeused;
                        String logMsg = String.format("jpg: %d timeused: %dms  fps: %d", finalBytes.length, (int) timeused, 1000 / timeused);
                        log.debug(logMsg);
                    } else {
                        byte[] subByte = Arrays.copyOfRange(buffer, cursor, length);
                        frameBody = Common.mergeArray(frameBody, subByte);
                        frameBodyLength -= (length - cursor);
                        readFrameBytes += (length - cursor);
                        cursor = length;
                    }
                }
            }
        }

        ////// banner
        int pid = 0;
        int realWidth = 0;
        int realHeight = 0;
        int virtualWidth = 0;
        int virtualHeight = 0;
        int orientation = 0;
        int quirks = 0;

        int parserBanner(int cursor, int ch) {
            switch (cursor) {
                case 0:
                    banner.setVersion(ch);
                    break;
                case 1:
                    bannerLen = ch;
                    banner.setLength(bannerLen);
                    break;
                case 2:
                case 3:
                case 4:
                case 5: {
                    pid += (ch << ((readn - 2) * 8));
                    if (cursor == 5)
                        banner.setPid(pid);
                    break;
                }
                case 6:
                case 7:
                case 8:
                case 9: {
                    realWidth += (ch << ((readn - 6) * 8));
                    if (cursor == 9)
                        banner.setReadWidth(realWidth);
                    break;
                }

                case 10:
                case 11:
                case 12:
                case 13:
                    realHeight += (ch << ((readn - 10) * 8));
                    if (cursor == 13)
                        banner.setReadHeight(realHeight);
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    virtualWidth += (ch << ((readn - 14) * 8));
                    if (cursor == 17)
                        banner.setVirtualWidth(virtualWidth);
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    virtualHeight += (ch << ((readn - 18) * 8));
                    if (cursor == 21)
                        banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    orientation = ch * 90;
                    banner.setOrientation(orientation);
                    break;
                case 23:
                    quirks = ch;
                    banner.setQuirks(quirks);
                    onBanner(banner);
                    break;
            }
            ++readn;
            ++cursor;
            return cursor;
        }
    }
}
