package com.testwa.distest.client.ios;

import com.testwa.distest.client.command.CommonProcessListener;
import com.testwa.distest.client.command.WdaProcessListener;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.port.WDAPortProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.openqa.selenium.net.UrlChecker;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stop.DestroyProcessStopper;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


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
    private static final String PROXY = "wdaproxy";

    private String udid;
    private IOSPhysicalSize size;
    private String wdaHome;

    private int port;

    private JWda jdwa;
    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private double percentX;
    private double percentY;

    private String lastX;
    private String lastY;
    private long lastTime;

    private Future<ProcessResult> future;
    private WdaProcessListener processListener;
    private boolean close = false;

    public WDAServer(String udid) {
        this.udid = udid;
        this.wdaHome = Config.getString("wda.home");
        this.port = WDAPortProvider.pullPort();
        this.size = IOSDeviceUtil.getSize(udid);
        this.percentX = (this.size.getPointWidth() * 1.0)/this.size.getPhsicalHeight();
        this.percentY = (this.size.getPointHeight() * 1.0)/this.size.getPhsicalHeight();
        this.processListener = new WdaProcessListener(this);
    }


    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        if(future != null) {
            if(future.isCancelled()){
                return false;
            }
            if(future.isDone()){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public void close() {
        this.close = true;
        if(future != null) {
            if(processListener.getProcess() != null) {
                DestroyProcessStopper.INSTANCE.stop(processListener.getProcess());
            }
        }
        WDAPortProvider.pushPort(this.port);
    }

    public boolean isClose() {
        return this.close;
    }

    public void serverStart() {
        try {
            while(IOSDeviceUtil.isOnline(this.udid)){
                if(IOSDeviceUtil.isUndetermined(this.udid)) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }else{
                    break;
                }
            }
            List<String> command = getWDAProxyCommand();
            log.info("拉起 ios wda proxy 服务 shellCommand: {}", command.toString().replace(",", ""));
            future = new ProcessExecutor()
                    .command(command)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            log.debug(line);
                        }
                    }).listener(processListener)
                    .start().getFuture();

            boolean running = waitUntilIsRunning();
            log.info("wda running {}", running);
            if(running) {
                this.jdwa = new JWda(getUrl(), udid);
            }else{
                log.error("wda 启动失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private List<String> getWDAProxyCommand() {
        List<String> command = new ArrayList<>();
        Path wdaProject = Paths.get(wdaHome);
        command.add(PROXY);
        command.add("-p");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(udid);
        command.add("-W");
        command.add(wdaProject.toString());
        // 启动debug
//        command.add("-d");
        return command;
    }


    private boolean waitUntilIsRunning() {
        try {
            final URL status = new URL(getUrl() + "/status");
            new UrlChecker().waitUntilAvailable(REAL_DEVICE_RUNNING_TIMEOUT, TimeUnit.MILLISECONDS, status);
            return true;
        } catch (UrlChecker.TimeoutException | MalformedURLException e) {
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

    public void restart() {
        serverStart();
    }
}
