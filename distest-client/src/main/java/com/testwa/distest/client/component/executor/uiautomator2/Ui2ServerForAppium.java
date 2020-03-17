package com.testwa.distest.client.component.executor.uiautomator2;import com.android.ddmlib.IDevice;import com.android.ddmlib.IShellOutputReceiver;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.appium.utils.Config;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.Closeable;import java.nio.file.Path;import java.nio.file.Paths;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: uiautomator 服务端 * @Author: wen * @Create: 2018-07-12 10:33 **/@Slf4jpublic class Ui2ServerForAppium extends Thread implements Closeable {    /** 执行 Ui2Server 的设备 */    private IDevice device;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    /** 是否重启 */    private AtomicBoolean restart = new AtomicBoolean(false);    public Ui2ServerForAppium(String deviceId){        super("ui2-appium");        this.device = AndroidHelper.getInstance().getAndroidDevice(deviceId).getDevice();    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    @Override    public void close() {        this.isRunning.set(false);        this.restart.set(true);    }    /**     * 重启     */    public void restart() {        this.isRunning.set(true);        this.restart.set(true);    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("appium Uiautomator2 服务已运行");        } else {            this.isRunning.set(true);        }        String resourcesPath = Config.getString("distest.agent.resources");        Path ui2 = Paths.get(resourcesPath, Constant.getAppiumUI2());        if(!ADBCommandUtils.isInstalledApp(device.getSerialNumber(), ui2.toString())){            ADBCommandUtils.installApp(device.getSerialNumber(), ui2.toString());        }        Path ui2Debug = Paths.get(resourcesPath, Constant.getAppiumUI2Debug());        if(!ADBCommandUtils.isInstalledApp(device.getSerialNumber(), ui2Debug.toString())){            ADBCommandUtils.installApp(device.getSerialNumber(), ui2Debug.toString());        }        super.start();    }    @Override    public void run() {        while (this.isRunning.get()) {            try {                String command = getCommand();                log.info("拉起 appium Uiautomator2 服务 shellCommand: {}", command);                device.executeShellCommand(command, new IShellOutputReceiver() {                    @Override                    public void addOutput(byte[] bytes, int i, int i1) {                        String ret = new String(bytes, i, i1);                        String[] split = ret.split("\n");                        for (String line : split) {                            if (StringUtils.isNotEmpty(line)) {                                log.info(line.trim());                            }                        }                    }                    @Override                    public void flush() {                    }                    @Override                    public boolean isCancelled() {                        boolean result = restart.get();                        restart.set(false);                        return result;                    }                }, Integer.MAX_VALUE, TimeUnit.DAYS);            } catch (Exception e) {                log.warn("{} appium Uiautomator2 服务运行异常, {}", device.getSerialNumber(), e.getMessage());            }        }        this.isRunning.set(false);        log.info("appium Uiautomator2 服务已关闭");    }    private String getCommand() {        return "am instrument -w io.appium.uiautomator2.server.test/androidx.test.runner.AndroidJUnitRunner";    }}