package com.testwa.distest.client.component.minicap;

import com.testwa.distest.client.component.wda.driver.IOSDriver;
import com.testwa.distest.client.component.wda.driver.OutputImageType;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ios屏幕同步服务 - wdascreen - 9100端口
 *
 * @author wen
 * @create 2019-06-18 23:19
 */
@Slf4j
public class IOSScreenLowerServer implements ScreenSubject {
    private static final String SCREEN_CMD = "ios-screen/smile.sh";
    // 保存base64队列
    private String udid;
    private String shFile;
    private StartedProcess mainProcess;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private List<ScreenProjectionObserver> observers = new ArrayList<>();

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public IOSScreenLowerServer(String udid, String resourcePath) {
        this.udid = udid;
        this.shFile = resourcePath + File.separator + SCREEN_CMD;

        ScheduledFuture future = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(mainProcess == null) {
                    return;
                }
                if(!mainProcess.getProcess().isAlive()) {
                    start();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void start() {
        this.isRunning.set(true);
        log.info("[{}] screen lower start", udid);
        try {
            mainProcess = new ProcessExecutor()
                    .command("/bin/sh", shFile, udid)
                    .readOutput(true)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            try {
                                BASE64Decoder decoder = new BASE64Decoder();
                                byte[] imgbytes = decoder.decodeBuffer(line);
                                notifyObservers(imgbytes);
                            } catch (Exception e) {

                            }finally {
                                long end = System.currentTimeMillis();
                            }
                        }
                    })
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }
    }

    public void close(){
        isRunning.set(false);
        CommandLineExecutor.processQuit(mainProcess);
    }

    public boolean isRunning() {
        return this.isRunning.get();
    }

    @Override
    public void registerObserver(ScreenProjectionObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(ScreenProjectionObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(byte[] image) {
        for(ScreenProjectionObserver observer : observers) {
            observer.frameImageChange(image);
        }
    }
}
