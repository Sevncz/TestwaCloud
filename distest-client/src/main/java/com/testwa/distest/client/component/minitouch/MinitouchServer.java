package com.testwa.distest.client.component.minitouch;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.TimeoutException;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.MinitouchServiceBuilder;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.android.AdbForward;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.port.MinitouchPortProvider;
import com.testwa.distest.client.exception.CommandFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by wen on 2017/4/19.
 */
@Slf4j
public class MinitouchServer extends Thread implements Closeable {
    /** android 临时文件存放目录 */
    private static final String ANDROID_TMP_DIR = "/data/local/tmp/";
    /** minitouch 临时存放目录 */
    private static final String MINITOUCH_TMP_DIR = ANDROID_TMP_DIR + "minitouch";
    /** minitouch-nopie 临时存放目录 */
    private static final String MINITOUCH_NOPIE_TMP_DIR = ANDROID_TMP_DIR + "minitouch-nopie";
    private static final String AB_NAME = "minitouch";

    private Integer port;
    private IDevice device;
    private String abi;
    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isReady = new AtomicBoolean(false);
    /** 是否重启 */
    private AtomicBoolean restart = new AtomicBoolean(false);

    /** resource 文件夹目录 */
    private String resourcePath;

    public MinitouchServer(String deviceId) {
        super("minitouch-server");
        this.resourcePath = Config.getString("distest.agent.resources");
        this.device = AndroidHelper.getInstance().getAndroidDevice(deviceId).getDevice();
    }

    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        return this.isRunning.get();
    }
    public boolean isReady() {
        return this.isReady.get();
    }

    @Override
    public void close() {
        this.isRunning.set(false);
        this.isReady.set(false);
        this.restart.set(true);
    }

    /**
     * 重启
     */
    public synchronized void restart() {
        this.isReady.set(false);
        this.restart.set(true);
    }

    @Override
    public synchronized void start() {
        if (this.isRunning.get()) {
            throw new IllegalStateException("Minicap服务已运行");
        } else {
            this.isRunning.set(true);
        }
        try {
            // push minicap
            String minicapPath = getMinitouchPath().toString();
            log.info("推送文件 local: {}, remote: {}", minicapPath, MINITOUCH_TMP_DIR);
            ADBCommandUtils.pushFile(device.getSerialNumber(), getResource(minicapPath), MINITOUCH_TMP_DIR, "777");

            // push minicap-nopie
            String minicapNopiePath = getMinitouchNopiePath().toString();
            log.info("推送文件 local: {}, remote: {}", minicapNopiePath, MINITOUCH_NOPIE_TMP_DIR);
            ADBCommandUtils.pushFile(device.getSerialNumber(), getResource(minicapNopiePath), MINITOUCH_NOPIE_TMP_DIR, "777");

            // forward port
            this.port = MinitouchPortProvider.pullPort();
            device.createForward(port, AB_NAME, IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            log.info("端口转发 tcp:{} localabstract:minicap", port);
        } catch (Exception e) {
            throw new IllegalStateException("Minicap服务启动失败");
        }
        super.start();
    }

    @Override
    public void run() {
        while (this.isRunning.get()) {
            try {
                // run minicap server
                String command = getCommand();
                log.info("拉起 Minitouch 服务 command: {}", command);
                device.executeShellCommand(command, new IShellOutputReceiver() {
                    @Override
                    public void addOutput(byte[] bytes, int i, int i1) {
                        String ret = new String(bytes, i, i1);
                        String[] split = ret.split("\n");
                        for (String line : split) {
                            if (StringUtils.isNotEmpty(line)) {
                                if(!isReady.get()) {
                                    isReady.set(true);
                                }
                                log.info("----minitouch----{}", line.trim());
                            }
                        }
                    }

                    @Override
                    public void flush() {
                    }

                    @Override
                    public boolean isCancelled() {
                        boolean result = restart.get();
                        restart.set(false);
                        return result;
                    }
                }, Integer.MAX_VALUE, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("{} MInitouch 服务运行异常, {}", device.getSerialNumber(), e.getMessage());
            }
        }
        try {
            device.removeForward(port, AB_NAME, IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            log.error("移除端口转发失败. port: {}", e, port);
        }
        this.isRunning.set(false);
        this.isReady.set(false);
        if(this.port != null) {
            MinitouchPortProvider.pushPort(this.port);
        }
        log.info("Minitouch 服务已关闭");
    }

    private String getResource(String name) {
        return this.resourcePath + File.separator + name;
    }

    /**
     * 获取cpu abi
     * @return cpu abi
     * @throws Exception 获取失败
     */
    protected String getAbi() throws Exception {
        if (abi == null) {
            abi = ADBCommandUtils.getAbi(device.getSerialNumber());
        }
        return abi;
    }

    /**
     * 获取执行的命令
     * @return shell命令
     */
    protected String getCommand() throws Exception {
        return "/data/local/tmp/minitouch";
    }

    /**
     * 获取minicap路径
     * @return minicap路径
     */
    protected Path getMinitouchPath() throws Exception {
        return Paths.get("minitouch", getAbi(), "minitouch");
    }

    /**
     * 获取minicap-nopie路径
     * @return minicap-nopie路径
     */
    protected Path getMinitouchNopiePath() throws Exception {
        return Paths.get("minitouch", getAbi(), "minitouch-nopie");
    }


    public int getPort() {
        return this.port;
    }
}
