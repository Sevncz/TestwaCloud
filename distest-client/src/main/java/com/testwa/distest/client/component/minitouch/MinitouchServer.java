package com.testwa.distest.client.component.minitouch;

import com.sun.jna.Platform;
import com.testwa.distest.client.android.*;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.util.CommonUtil;
import com.testwa.distest.client.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
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
    }

    public synchronized void start() {
        if(this.mainProcess != null && this.mainProcess.getProcess().isAlive()) {
            log.error("[{}] 已经在运行中", deviceId);
            return;
        }
        try {
            // push minicap
            if(!checkMinitouchInstallation(deviceId)) {
                String minicapPath = getMinitouchPath().toString();
                log.info("[{}] 推送文件 local: {}, remote: {}", deviceId, minicapPath, MINITOUCH_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapPath), MINITOUCH_TMP_DIR);
//            ADBTools.chmod(deviceId, MINITOUCH_TMP_DIR, "777");
                ADBCommandUtils.pushFile(deviceId, getResource(minicapPath), MINITOUCH_TMP_DIR, "777");

                // push minicap-nopie
                String minicapNopiePath = getMinitouchNopiePath().toString();
                log.info("[{}] 推送文件 local: {}, remote: {}", deviceId, minicapNopiePath, MINITOUCH_NOPIE_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapNopiePath), MINITOUCH_NOPIE_TMP_DIR);
//            ADBTools.chmod(deviceId, MINITOUCH_NOPIE_TMP_DIR, "777");
                ADBCommandUtils.pushFile(deviceId, getResource(minicapNopiePath), MINITOUCH_NOPIE_TMP_DIR, "777");
            }else{
                log.info("[{}] 已安装", deviceId);
            }

            int processId = getMinitouchProcessID(deviceId);
            ADBTools.killProcess(deviceId, processId);

            String[] shellCommand = ADBTools.buildAdbShell(deviceId);
            String command = getCommand();
            String[] mainCommand =  ArrayUtils.addAll(shellCommand, command);

            this.mainProcess = new ProcessExecutor()
                    .command(mainCommand)
                    .readOutput(true)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String s) {
                            log.info("[{}] minitouch out: {}", deviceId, s);
                        }
                    })
                    .start();
        } catch (Exception e) {
            release();
            throw new IllegalStateException("[" + deviceId + "] 启动失败", e);
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

    public boolean checkMinitouchInstallation(String deviceId) {
        String cmd = "ls /data/local/tmp/minitouch /data/local/tmp/minitouch-nopie";
        String ret = ADBTools.shell(deviceId, cmd);
        StringTokenizer st = new StringTokenizer(ret);

        int index = 0;
        while (st.hasMoreElements()) {
            String token = st.nextToken();

            if (index == 0) {
                if (token.equals("/data/local/tmp/minitouch")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else if(index == 1) {
                if (token.equals("/data/local/tmp/minitouch-nopie")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else {
                log.error("[{" + deviceId + "}] Unexpected token: " + token + ", the whole result:" + ret);
                return false;
            }
        }

        return index == 2;
    }

    public int getMinitouchProcessID(String deviceId) {
        String cmd = ADBTools.getPsCommand(deviceId) + "|grep minitouch";
        String ret;
        try {
            ret = ADBTools.shell(deviceId, cmd);
            if (StringUtils.isEmpty(ret)) {
                return -1;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return -1;
        }
        ret = ret.trim();
        return CommonUtil.resolveProcessID(ret, "minitouch");
    }

    public static void main(String[] args) throws InterruptedException {
        String deviceId = "b5534977";
        String resourcePath = "/Users/wen/IdeaProjects/distest/distest-client/bin/resources";
        MinitouchServer server = new MinitouchServer(deviceId, resourcePath);
        server.start();
        TimeUnit.MILLISECONDS.sleep(200);
        MinitouchClient client = new MinitouchClient(deviceId);
        client.start();
        TimeUnit.SECONDS.sleep(120);
    }
}
