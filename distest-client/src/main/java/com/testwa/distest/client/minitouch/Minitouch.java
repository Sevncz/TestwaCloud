package com.testwa.distest.client.minitouch;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.TimeoutException;
import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.MinicapServiceBuilder;
import com.testwa.core.service.MinitouchServiceBuilder;
import com.testwa.core.utils.Common;
import com.testwa.distest.client.android.AdbForward;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.control.port.TouchPortProvider;
import com.testwa.distest.client.minicap.Minicap;
import com.testwa.distest.client.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wen on 2017/4/19.
 */
public class Minitouch {
    private static Logger log = LoggerFactory.getLogger(Minitouch.class);

    private List<MinitouchListener> listenerList = new ArrayList<MinitouchListener>();

//    private IDevice device;
    private AndroidDevice device;
    private String resourcesPath;
    private Thread minitouchInitialThread;
    private Socket minitouchSocket;
    private OutputStream minitouchOutputStream;
    private AdbDriverService service;
    private AdbForward forward;
    private static String BIN = "";

    private boolean isRunnging = true;

    public static void installMinitouch(AndroidDevice device, String resourcesPath) throws MinitouchInstallException {
        if (device == null) {
            throw new MinitouchInstallException("device can't be null");
        }
        IDevice iDevice = device.getDevice();
        String sdk = iDevice.getProperty(Constant.PROP_SDK);
        String abi = iDevice.getProperty(Constant.PROP_ABI);

        if (StringUtils.isEmpty(sdk) || StringUtils.isEmpty(abi)) {
            throw new MinitouchInstallException("cant not get device info. please check device is connected");
        }

        sdk = sdk.trim();
        abi = abi.trim();
        Integer sdkvalue = Integer.parseInt(sdk);
        if (sdkvalue >= 16) {
            BIN = Constant.MINITOUCH_BIN;
        } else {
            BIN = Constant.MINITOUCH_NOPIE;
        }

        File minitouch_bin = new File(resourcesPath + File.separator + Constant.getMinitouchBin(abi, BIN));
        if (!minitouch_bin.exists()) {
            throw new MinitouchInstallException("File: " + minitouch_bin.getAbsolutePath() + " not exists!");
        }
        try {
            iDevice.pushFile(minitouch_bin.getAbsolutePath(), Constant.MINITOUCH_DIR + "/" + BIN);
        } catch (Exception e) {
            throw new MinitouchInstallException(e.getMessage());
        }

        AndroidHelper.getInstance().executeShellCommand(iDevice, "chmod 777 " + Constant.MINITOUCH_DIR + "/" + BIN);
    }

    public Minitouch(AndroidDevice device, String resourcesPath) {
        this.device = device;
        this.resourcesPath = resourcesPath;
        int install = 3;
        while (install > 0) {
            try {
                installMinitouch(device, resourcesPath);
                Thread.sleep(1000);
                break;
            } catch (MinitouchInstallException e) {
                install--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Minitouch(String serialNumber, String resourcesPath) {
        this(AndroidHelper.getInstance().getAndroidDevice(serialNumber), resourcesPath);
    }

    public void addEventListener(MinitouchListener listener) {
        if (listener != null) {
            this.listenerList.add(listener);
        }
    }

    public AdbForward createForward() {
        forward = generateForwardInfo();
        int tryTime = 10;
        while(tryTime >= 0){
            try {
                device.getDevice().createForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);

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
            device.getDevice().removeForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (AdbCommandRejectedException e) {
            log.info("removeForward: AdbCommandRejectedException, {}", e.getMessage());
        } catch (IOException e) {
            log.error("removeForward: IOException, {}", e.getMessage());
        } catch (TimeoutException e) {
            log.error("removeForward: TimeoutException, {}", e.getMessage());
        }
    }

    public void start(){
        /*
         * BIN有可能为空字符串, 在这里判断，如果为空，再install下
         */
        if(StringUtils.isEmpty(BIN)){
            try {
                installMinitouch(device, resourcesPath);
            } catch (MinitouchInstallException e) {
                e.printStackTrace();
            }
        }
        isRunnging = true;
        AdbForward forward = createForward();
        if(forward == null){
            return;
        }
        service = new MinitouchServiceBuilder()
                .whithDeviceId(device.getSerialNumber())
                .whithExecute(Constant.MINITOUCH_DIR + "/" + BIN)
                .whithName(forward.getLocalabstract())
                .build();
        service.start();
        minitouchInitialThread = new Thread(new StartInitial("127.0.0.1", forward.getPort()));
        minitouchInitialThread.start();
        log.info("minitouch forward port {}", forward.getPort());
    }

    public void kill() {
        onClose();
        this.isRunnging = false;

        if (service != null) {
            service.stop();
        }
        // 关闭socket
        if (minitouchSocket != null && minitouchSocket.isConnected()) {
            try {
                minitouchSocket.close();
            } catch (IOException e) {
            }
            minitouchSocket = null;
        }

    }

    public void sendEvent(String str) {
        if (minitouchOutputStream == null) {
            return;
        }
        try {
            minitouchOutputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendKeyEvent(int k) {
        AndroidHelper.getInstance().executeShellCommand(device.getDevice(), "input keyevent " + k);
    }

    public void inputText(String str) {
        AndroidHelper.getInstance().executeShellCommand(device.getDevice(), "input text " + str);
    }

    /**
     * 生成forward信息
     */
    private AdbForward generateForwardInfo() {
        AdbForward[] forwards = AndroidHelper.getInstance().getForwardList();
        // serial_touch_number
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

        String forwardStr = String.format("%s_touch_%d", device.getSerialNumber(), maxNumber);
        int freePort = TouchPortProvider.pullPort();
        AdbForward forward = new AdbForward(device.getSerialNumber(), freePort, forwardStr);
        return forward;
    }


    class StartInitial implements Runnable {
        private String host;
        private int port;

        public StartInitial(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void run() {
            log.info("StartInitial schedule start, host: {}, port: {}", this.host, this.port);
            int tryTime = 20;
            byte[] bytes = null;
            // 连接minicap启动的服务
            while (isRunnging) {
                Socket socket = null;
                bytes = new byte[256];
                try {
                    socket = new Socket(host, port);
                    InputStream inputStream = socket.getInputStream();
                    OutputStream outputStream = socket.getOutputStream();
                    int n = inputStream.read(bytes);
                    if (n == -1) {
                        Thread.sleep(1000);
                        log.info("minitouch socket close, retry again");
                        socket.close();
                    } else {
                        log.info("minitouch ---------- start ");
                        minitouchSocket = socket;
                        minitouchOutputStream = outputStream;
                        onStartup(true);
                        break;
                    }
                } catch (Exception ex) {
                    log.error("minitouch connect error, {}, {}", this.host, this.port, ex);
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                tryTime--;
                if (tryTime == 0) {
                    onStartup(false);
                    break;
                }
            }
        }

    }

    private void onStartup(boolean success) {
        for (MinitouchListener listener : listenerList) {
            listener.onStartup(this, success);
        }
    }

    private void onClose() {
        for (MinitouchListener listener : listenerList) {
            listener.onClose(this);
        }
        removeForward(forward);
    }
}
