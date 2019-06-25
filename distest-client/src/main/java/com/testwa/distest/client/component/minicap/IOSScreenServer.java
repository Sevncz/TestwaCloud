package com.testwa.distest.client.component.minicap;

import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.util.CommonProcessListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ios屏幕同步服务
 *
 * @author wen
 * @create 2019-06-18 23:19
 */
@Slf4j
public class IOSScreenServer {
    private static final String SCREEN_CMD = "ios-screen/smile.sh";
    // 保存base64队列
    private ConcurrentLinkedQueue<String> queue;
    private String udid;
    private String shFile;
    private StartedProcess mainProcess;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture handler;

    public IOSScreenServer(String udid, String resourcePath) {
        this.udid = udid;
        this.shFile = resourcePath + File.separator + SCREEN_CMD;
        this.queue = new ConcurrentLinkedQueue<>();

        handler = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(isRunning.get()) {
                    if(mainProcess == null) {
                        return;
                    }
                    if(!mainProcess.getProcess().isAlive()) {
                        start();
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void start() {
        log.info("[Start idevicescreenshot command] {}", udid);
        List<String> commandLine = Arrays.asList("/bin/sh", shFile, udid);
        CommonProcessListener processListener = new CommonProcessListener(String.join(" ", commandLine));
        try {
            mainProcess = new ProcessExecutor()
                    .command(commandLine)
                    .readOutput(true)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            if(queue.size() >= 10) {
                                // 丢弃一张
                                queue.poll();
                            }
                            queue.offer(line);
                        }
                    })
                    .addListener(processListener)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }
        this.isRunning.set(true);
    }

    public void close(){
        isRunning.set(false);
        handler.cancel(true);
        CommandLineExecutor.processQuit(mainProcess);
    }

    public boolean isRunning() {
        return this.isRunning.get();
    }

    public byte[] take() {
        long start = System.currentTimeMillis();
        String base64Image = queue.poll();
        if(StringUtils.isBlank(base64Image)) {
            return null;
        }
        // create a buffered image
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            return decoder.decodeBuffer(base64Image);
        } catch (Exception e) {

        }finally {
            long end = System.currentTimeMillis();
            log.debug("图片花费时间{}ms", end - start);
        }
        return null;
    }

}
