package com.testwa.distest.client.component.minicap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.support.OkHttpUtil;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.util.CommonProcessListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ios屏幕同步服务 - wdascreen - http
 *
 * @author wen
 * @create 2019-06-18 23:19
 */
@Slf4j
public class IOSScreenServer2 extends Thread implements Closeable {
    private String host = "127.0.0.1";
    private int port;
    private Socket socket;

    /** 队列大小 */
    private int queueSize = 50;
    /** 调用take的线程 */
    private Thread takeThread;
    /** 存放图片队列 */
    private BlockingQueue<byte[]> frameQueue;

    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public IOSScreenServer2(int port) {
        super("wda-client");
        this.port = port;
        init();
    }

    /**
     * 获取帧，如果没有则阻塞
     * @return 帧
     * @throws InterruptedException 阻塞中断
     */
    public synchronized byte[] take() throws InterruptedException {
        checkClosed();
        takeThread = Thread.currentThread();
        return frameQueue.take();
    }

    /**
     * 是否运行
     * @return true 已运行 false 未运行
     */
    public boolean isRunning() {
        return this.isRunning.get();
    }


    @Override
    public void close() throws IOException {
        this.isRunning.set(false);
        IOUtils.closeQuietly(this.socket);
        this.interrupt();
    }

    @Override
    public synchronized void start() {
        if (this.isRunning.get()) {
            throw new IllegalStateException("WDA 客户端已运行");
        } else {
            this.isRunning.set(true);
        }
        super.start();
    }

    @Override
    public void run() {
        // 连接WDA服务
        log.info("WDA 客户端启动中 port: {}......", port);
//        InputStream inputStream = null;
//        boolean isConnected = false;
//        while (!isConnected){
//            try {
//                this.socket = new Socket(host, port);
//                this.socket.setSoTimeout(10*1000);
//                isConnected = true;
//            } catch (IOException e) {
//                log.error("与WDA服务端连接失败，稍后再试......");
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//        log.info("与WDA服务端连接成功");
//        while (isRunning.get()) {
//            try {
//                inputStream = socket.getInputStream();9000
//                int len = 4096;
//                byte[] buffer;
//                buffer = new byte[len];
//                int realLen = inputStream.read(buffer);
//                if (buffer.length != realLen && realLen >= 0) {
//                    buffer = subByteArray(buffer, 0, realLen);
//
//                }
//                if(realLen >= 0) {
//                    offer(buffer);
//                }
//            } catch (Exception e) {
////                log.warn("Minicap客户端运行错误 {}", e.getMessage());
//            }
//        }
//        IOUtils.closeQuietly(this.socket);

        while (isRunning.get()) {
            try {
                String result = OkHttpUtil.get("http://127.0.0.1:" + port + "/screenshot", null);
                if (StringUtils.isNotEmpty(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    String pngBase64Value = jsonObject.getString("value");
                    if (StringUtils.isNotEmpty(pngBase64Value)) {
                        byte[] imageByte;
                        BASE64Decoder decoder = new BASE64Decoder();
                        try {
                            imageByte = decoder.decodeBuffer(pngBase64Value);
                            offer(imageByte);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch (Exception e) {
                OkHttpClient okHttpClient = ApplicationContextUtil.getBean(OkHttpClient.class);
                int connectionCount = okHttpClient.connectionPool().connectionCount();
                int idleCount = okHttpClient.connectionPool().idleConnectionCount();
                log.error("[OKHttp3 connect pool] connection: {}, idle: {}", connectionCount, idleCount);
                e.printStackTrace();
            }
        }

    }

    /**
     * 将一帧放入到队列
     */
    protected void offer(byte[] frame) {
        if (frameQueue.size() >= queueSize) {
            frameQueue.poll();
            log.debug("存放帧的队列已满，将会抛弃最旧的一帧");
        }
        long start = System.currentTimeMillis();
        frameQueue.offer(frame);
        long end = System.currentTimeMillis();
        log.debug(String.valueOf(end -  start));

    }

    /**
     * 检查是否关闭
     */
    protected void checkClosed() {
        if (!this.isRunning.get()) {
            throw new IllegalStateException("Minicap客户端已关闭");
        }
    }
    /**
     * 初始化
     */
    protected void init() {
        this.frameQueue = new LinkedBlockingQueue<>(queueSize);
    }

    public static void main(String[] args) {
//        OkHttpUtil.get("http://127.0.0.1:9001", null);
    }

}
