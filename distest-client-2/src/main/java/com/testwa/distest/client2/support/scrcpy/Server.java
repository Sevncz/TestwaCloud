package com.testwa.distest.client2.support.scrcpy;

import com.android.ddmlib.*;
import com.testwa.distest.client2.support.android.AndroidDeviceStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scrcpy-server.jar 启动类
 * @author wen
 * @create 2019-05-08 14:10
 */
@Slf4j
@Data
class Server extends Thread implements Closeable {

    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isReady = new AtomicBoolean(false);

    private IDevice device;
    private String cmd;


    public Server(String deviceId, String cmd) {
        super("scrcpy-server");
        this.device = AndroidDeviceStore.getInstance().get(deviceId);
        this.cmd = cmd;
    }

    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        return this.isRunning.get();
    }

    /**
     * 是否已启动
     * @return
     */
    public boolean isReady() {
        return this.isReady.get();
    }


    @Override
    public void close() {
        this.isRunning.set(false);
        this.isReady.set(false);
    }

    @Override
    public synchronized void start() {
        if (this.isRunning.get()) {
            throw new IllegalStateException("Scrcpy服务已运行");
        }else{
            this.isRunning.set(true);
        }
        super.start();
    }

    @Override
    public void run() {
        try {
            // run scrcpy server
            log.info("start scrcpy server command: {}", this.cmd);
            device.executeShellCommand(this.cmd, new IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] bytes, int i, int i1) {
                }

                @Override
                public void flush() {

                }

                @Override
                public boolean isCancelled() {
                    return false;
                }
            }, Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("{} scrcpy 服务运行异常, {}", device.getSerialNumber(), e.getMessage());
        }finally {
            this.isRunning.set(false);
            this.isReady.set(false);
        }

        log.info("scrcpy服务结束");
    }

}
