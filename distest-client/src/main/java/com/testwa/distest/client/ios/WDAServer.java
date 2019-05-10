package com.testwa.distest.client.ios;

import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.port.WDAPortProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.openqa.selenium.net.UrlChecker;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

/**
 * @author wen
 * @create 2019-04-17 20:04
 */
@Slf4j
public class WDAServer{
    private static final int MAX_REAL_DEVICE_RESTART_RETRIES = 1;
    private static final Long REAL_DEVICE_RUNNING_TIMEOUT = 4*60*1000L;
    private static final Long RESTART_TIMEOUT = 1*60*1000L;

    private static final String WDA_PROJECT = "WebDriverAgent.xcodeproj";
    private static final String WDA_SCHEME = "WebDriverAgentRunner";
    private static final String XCODE = "/usr/bin/xcodebuild";
    private static final String PROXY = "/usr/local/bin/wdaproxy";

    private String udid;
    private IOSPhysicalSize size;
    private String wdaHome;

    private int port;

    private UTF8CommonExecs proxyExecs;
    private JWda jdwa;
    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private double percentX;
    private double percentY;

    private String lastX;
    private String lastY;
    private Long lastTime;

    public WDAServer(String udid) {
        this.udid = udid;
        this.wdaHome = Config.getString("wda.home");
        this.port = WDAPortProvider.pullPort();
        this.size = IOSDeviceUtil.getSize(udid);
        this.percentX = (this.size.getPointWidth() * 1.0)/this.size.getPhsicalHeight();
        this.percentY = (this.size.getPointHeight() * 1.0)/this.size.getPhsicalHeight();
    }

    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        return this.isRunning.get();
    }

    public void serverStart() {
        try {
            CommandLine commandLine2 = getWDAProxyCommand();
            log.info("拉起 ios wda proxy 服务 command: {}", commandLine2.toString().replace(",", ""));
            proxyExecs = new UTF8CommonExecs(commandLine2);
            proxyExecs.setTimeout(INFINITE_TIMEOUT);
            proxyExecs.asyncexec();
            boolean running = waitUntilIsRunning();
            log.info("wda running {}", running);
            if(running) {
                this.jdwa = new JWda(getUrl(), udid);
            }else{
                log.error("wda 启动失败");
            }
        } catch (Exception e) {
            String out = proxyExecs.getOutput();
            String error = proxyExecs.getError();
            log.warn("iOS {} wda服务运行异常, {} {}", udid, out, error);
        }
    }

    private CommandLine getWDACommand() {
        Path wdaProject = Paths.get(wdaHome, WDA_PROJECT);
        if(!Files.exists(wdaProject)) {
            return null;
        }

        String targetVersion = IOSDeviceUtil.getProductVersion(this.udid);
        String id = String.format("id=%s", this.udid);
        String target = String.format("IPHONEOS_DEPLOYMENT_TARGET=%s", targetVersion);

        CommandLine commandLine = new CommandLine(XCODE);
        commandLine.addArgument("build-for-testing");
        commandLine.addArgument("test-without-building");
        commandLine.addArgument("-project");
        commandLine.addArgument(wdaProject.toString());
        commandLine.addArgument("-scheme");
        commandLine.addArgument(WDA_SCHEME);
        commandLine.addArgument("-destination");
        commandLine.addArgument(id);
        commandLine.addArgument(target);
        commandLine.addArgument("-allowProvisioningUpdates");
        return commandLine;
    }

    private CommandLine getWDAProxyCommand() {
        Path wdaProject = Paths.get(wdaHome);
        CommandLine commandLine = new CommandLine(PROXY);
        commandLine.addArgument("-p");
        commandLine.addArgument(String.valueOf(port));
        commandLine.addArgument("-u");
        commandLine.addArgument(udid);
        commandLine.addArgument("-W");
        commandLine.addArgument(wdaProject.toString());
        return commandLine;
    }


    private boolean waitUntilIsRunning() throws Exception {
        final URL status = new URL(getUrl() + "/status");
        try {
            new UrlChecker().waitUntilAvailable(REAL_DEVICE_RUNNING_TIMEOUT, TimeUnit.MILLISECONDS, status);
            return true;
        } catch (UrlChecker.TimeoutException e) {
            return false;
        }
    }

    public String getUrl() {
        return "http://127.0.0.1:"+ port;
    }

    public void tap(String x, String y) {
        if(this.jdwa != null) {
            this.jdwa.tap("30", "20");
        }
    }

    public void tap(String command) {
        if(this.jdwa != null) {
            int x;
            int y;
            // m <contact> <x> <y> <pressure>
            // d <contact> <x> <y> <pressure>
            if(command.startsWith("d") ){
                String[] m = command.trim().split("\\s+");
                try{
                    x = Integer.parseInt(m[2]);
                    y = Integer.parseInt(m[3]);

                    double xd = x * percentX;
                    double yd = y * percentY;
                    this.lastX = String.valueOf(xd);
                    this.lastY = String.valueOf(yd);
                    this.lastTime = System.currentTimeMillis();
                    this.jdwa.tap(lastX, lastY);
                }catch (NumberFormatException e){
                    log.error("point str error, {}", command);
                }
            }
            if(command.startsWith("m") ){
                String[] m = command.trim().split("\\s+");
                try{
                    x = Integer.parseInt(m[2]);
                    y = Integer.parseInt(m[3]);

                    double xd = x * percentX;
                    double yd = y * percentY;
                    this.jdwa.swipe(lastX, lastY, String.valueOf(xd), String.valueOf(yd), 0);

                }catch (NumberFormatException e){
                    log.error("point str error, {}", command);
                }
            }
        }
    }

    public void home() {
        if(this.jdwa != null) {
            this.jdwa.home();
        }
    }
}
