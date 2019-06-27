package com.testwa.distest.client.component.minitouch;

import com.testwa.distest.client.android.*;
import com.testwa.distest.client.component.port.MinitouchPortProvider;
import com.testwa.distest.client.util.CommandLineExecutor;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.StartedProcess;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


/**
 * Created by wen on 2017/4/19.
 */
@Slf4j
public class MinitouchServer {
    /** android 临时文件存放目录 */
    private static final String ANDROID_TMP_DIR = "/data/local/tmp/";
    /** minitouch 临时存放目录 */
    private static final String MINITOUCH_TMP_DIR = ANDROID_TMP_DIR + "minitouch";
    /** minitouch-nopie 临时存放目录 */
    private static final String MINITOUCH_NOPIE_TMP_DIR = ANDROID_TMP_DIR + "minitouch-nopie";
    private static final String AB_NAME = "minitouch";

    private Integer port;
    private String deviceId;
    private String abi;

    /** resource 文件夹目录 */
    private String resourcePath;

    private StartedProcess mainProcess;

    public MinitouchServer(String deviceId, String resourcePath) {
        this.resourcePath = resourcePath;
        this.deviceId = deviceId;
    }

    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        if(this.mainProcess != null) {
            return mainProcess.getProcess().isAlive();
        }
        return false;
    }

    public void close() {
        release();
    }

    /**
     * 重启
     */
    public synchronized void restart() {
        close();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {

        }
        start();
    }

    private void release() {
        if(this.mainProcess != null) {
            CommandLineExecutor.processQuit(mainProcess);
        }
        if(this.port != null) {
            ADBTools.forwardRemove(deviceId, this.port);
            MinitouchPortProvider.pushPort(this.port);
        }
    }

    public synchronized void start() {
        if(this.mainProcess != null && this.mainProcess.getProcess().isAlive()) {
            log.error("minitouch已经在运行中");
            return;
        }
        try {
            // push minicap
            String minicapPath = getMinitouchPath().toString();
            log.info("推送文件 local: {}, remote: {}", minicapPath, MINITOUCH_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapPath), MINITOUCH_TMP_DIR);
//            ADBTools.chmod(deviceId, MINITOUCH_TMP_DIR, "777");
            ADBCommandUtils.pushFile(deviceId, getResource(minicapPath), MINITOUCH_TMP_DIR, "777");

            // push minicap-nopie
            String minicapNopiePath = getMinitouchNopiePath().toString();
            log.info("推送文件 local: {}, remote: {}", minicapNopiePath, MINITOUCH_NOPIE_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapNopiePath), MINITOUCH_NOPIE_TMP_DIR);
//            ADBTools.chmod(deviceId, MINITOUCH_NOPIE_TMP_DIR, "777");
            ADBCommandUtils.pushFile(deviceId, getResource(minicapNopiePath), MINITOUCH_NOPIE_TMP_DIR, "777");

            // forward port
            this.port = MinitouchPortProvider.pullPort();
            ADBTools.forward(deviceId, this.port, AB_NAME);
            log.info("端口转发 tcp:{} localabstract:minicap", port);

            String command = getCommand();
            mainProcess = ADBTools.asyncCommandShell(deviceId, command);

        } catch (Exception e) {
            release();
            throw new IllegalStateException("Minitouch服务启动失败");
        }
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
            abi = ADBTools.getAbi(deviceId);
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
