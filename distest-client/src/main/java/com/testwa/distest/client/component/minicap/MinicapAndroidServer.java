package com.testwa.distest.client.component.minicap;

import com.sun.jna.Platform;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.android.ADBTools;
import com.testwa.distest.client.android.PhysicalSize;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.util.CommonUtil;
import com.testwa.distest.client.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.StartedProcess;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.concurrent.*;

/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class MinicapAndroidServer {
    /** android 临时文件存放目录 */
    private static final String ANDROID_TMP_DIR = "/data/local/tmp/";
    /** minicap 临时存放目录 */
    private static final String MINICAP_TMP_DIR = ANDROID_TMP_DIR + "minicap";
    /** minicap-nopie 临时存放目录 */
    private static final String MINICAP_NOPIE_TMP_DIR = ANDROID_TMP_DIR + "minicap-nopie";
    /** minicap.so 临时存放目录 */
    private static final String MINICAP_SO_TMP_DIR = ANDROID_TMP_DIR + "minicap.so";
    /** 文件权限 */
    private static final String MODE = "777";
    private static final String SOCK_NAME = "minicap";

    /** cpu abi */
    private String abi;
    /** sdk api */
    private int api;
    /** 屏幕尺寸 */
    private PhysicalSize size;
    /** 端口 */
    private Integer port;
    /** 旋转 0|90|180|270 */
    private int rotate = 0;
    /** -Q <value>: JPEG quality (0-100) */
    private int quality = 100;

    /** resource 文件夹目录 */
    private String resourcePath;

    private int retry = 10;

    private StartedProcess mainProcess;
    private String deviceId;

    public MinicapAndroidServer(String deviceId, String resourcePath) {
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
        }
    }

    public synchronized void start() {
        if(this.mainProcess != null && this.mainProcess.getProcess().isAlive()) {
            log.error("minicap已经在运行中");
            return;
        }
        try {
            // push minicap
            if(!checkMinicapInstallation(deviceId)) {
                String minicapPath = getMinicapPath().toString();
                log.info("推送文件 local: {}, remote: {}", minicapPath, MINICAP_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapPath), MINICAP_TMP_DIR);
//            ADBTools.chmod(deviceId, MINICAP_TMP_DIR, MODE);
                ADBCommandUtils.pushFile(deviceId, getResource(minicapPath), MINICAP_TMP_DIR, MODE);

                // push minicap.so
                String minicapSoPath = getMinicapSoPath().toString();
                log.info("推送文件 local: {}, remote: {}", minicapSoPath, MINICAP_SO_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapSoPath), MINICAP_SO_TMP_DIR);
//            ADBTools.chmod(deviceId, MINICAP_SO_TMP_DIR, MODE);
                ADBCommandUtils.pushFile(deviceId, getResource(minicapSoPath), MINICAP_SO_TMP_DIR, MODE);

                // push minicap-nopie
                String minicapNopiePath = getMinicapNopiePath().toString();
                log.info("推送文件 local: {}, remote: {}", minicapNopiePath, MINICAP_NOPIE_TMP_DIR);
//            ADBTools.pushFile(deviceId, getResource(minicapNopiePath), MINICAP_NOPIE_TMP_DIR);
//            ADBTools.chmod(deviceId, MINICAP_NOPIE_TMP_DIR, MODE);
                ADBCommandUtils.pushFile(deviceId, getResource(minicapNopiePath), MINICAP_NOPIE_TMP_DIR, MODE);
            }else{
                log.info("[Minitouch 已安装]");
            }

            int processId = getMinicapProcessID(deviceId);
            ADBTools.killProcess(deviceId, processId);

            // forward port
//            this.port = MinicapPortProvider.pullPort();
            this.port = PortUtil.getAvailablePort();
            boolean success = ADBTools.forward(deviceId, port, SOCK_NAME);
            log.info("[Minicap 端口转发] {} tcp:{} localabstract:minicap", success, port);

            String command = getCommand();
            this.mainProcess = ADBTools.asyncCommandShell(deviceId, command);

        } catch (Exception e) {
            release();
            throw new IllegalStateException("Minicap服务启动失败", e);
        }
    }

    private String getResource(String name) {
        return this.resourcePath + File.separator + name;
    }

    /**
     * 获取cpu abi
     * @return cpu abi
     */
    protected String getAbi() {
        if (abi == null) {
            abi = ADBTools.getAbi(deviceId);
        }
        return abi;
    }

    /**
     * 获取sdk api
     * @return sdk api
     * @ 获取失败
     */
    protected int getApi()  {
        String apiStr = ADBTools.getApi(deviceId);
        if(StringUtils.isNotBlank(apiStr)) {
            this.api = Integer.parseInt(apiStr);
        }
        return this.api;
    }

    /**
     * 获取屏幕支持
     * @return PhysicalSize
     * @ 获取失败
     */
    protected PhysicalSize getSize() {
        if (size == null) {
            size = ADBTools.getPhysicalSize(deviceId);
        }
        return size;
    }

    /**
     * Get display projection (<w>x<h>@<w>x<h>/{0|90|180|270})
     * @return Display projection
     */
    protected String getProjection()  {
        PhysicalSize size = getSize();
        int width = 480;
        int height = (int) ((width*1.0/size.getWidth())*size.getHeight());
        return String.format("%sx%s@%sx%s/%s", size.getWidth(), size.getHeight(),
                width, height, rotate);
    }

    /**
     * 获取执行的命令
     * @return shell命令
     */
    protected String getCommand()  {
        return String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s -Q %s", getProjection(), quality);
    }

    /**
     * 获取minicap路径
     * @return minicap路径
     */
    protected Path getMinicapPath()  {
        return Paths.get("minicap", "libs", getAbi(), "minicap");
    }

    /**
     * 获取minicap-nopie路径
     * @return minicap-nopie路径
     */
    protected Path getMinicapNopiePath()  {
        return Paths.get("minicap", "libs", getAbi(), "minicap-nopie");
    }

    /**
     * 获取minicap.so路径
     * @return minicap.so路径
     */
    protected Path getMinicapSoPath()  {
        return Paths.get("minicap", "shared", "android-" + getApi(), getAbi(), "minicap.so");
    }

    public int getPort() {
        return this.port;
    }

    /**
     * 设置旋转角度，默认为0不旋转
     * @param rotate 旋转角度
     */
    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    /**
     * 设置图片质量，默认为100最高质量
     * @param quality 图片质量
     */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean checkMinicapInstallation(String deviceId) {
        String cmd = "ls /data/local/tmp/minicap /data/local/tmp/minicap.so";
        String ret = ADBTools.shell(deviceId, cmd);
        StringTokenizer st = new StringTokenizer(ret);

        int index = 0;
        while (st.hasMoreElements()) {
            String token = st.nextToken();

            if (index == 0) {
                if (token.equals("/data/local/tmp/minicap")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else if(index == 1) {
                if (token.equals("/data/local/tmp/minicap.so")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else {
                log.error("Unexpected token: " + token + ", the whole result:" + ret);
                return false;
            }
        }

        return index == 2;
    }

    public int getMinicapProcessID(String deviceId) {
        String cmd = ADBTools.getPsCommand(deviceId) + "|grep minicap";
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
        return CommonUtil.resolveProcessID(ret, "minicap");
    }


}
