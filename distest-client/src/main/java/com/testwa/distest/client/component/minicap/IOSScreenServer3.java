package com.testwa.distest.client.component.minicap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.support.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ios屏幕同步服务 - wdascreen - 9100端口
 *
 * @author wen
 * @create 2019-06-18 23:19
 */
@Slf4j
public class IOSScreenServer3 extends Thread implements Closeable {
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

    public IOSScreenServer3(int port) {
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
        InputStream inputStream = null;
        boolean isConnected = false;
        while (!isConnected){
            try {
                this.socket = new Socket(host, port);
                this.socket.setSoTimeout(10*1000);
                isConnected = true;
            } catch (IOException e) {
                log.error("与WDA服务端连接失败，稍后再试......");
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        log.info("与WDA服务端连接成功");
        while (isRunning.get()) {
            try {
                inputStream = socket.getInputStream();
//                ImageInputStream imageInputStream = ImageIO.createImageInputStream(socket.getInputStream());
//                BufferedImage img = ImageIO.read(imageInputStream);
//                if(img == null) {
//                    continue;
//                }
//                ImageIO.write(img, "jpg", new File("/Users/wen/Downloads/a.png") );
//                InputStream is;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readBytes = -1;

                while((readBytes = inputStream.read(buffer)) > 1){
                    baos.write(buffer,0,readBytes);
                }

                byte[] responseArray = baos.toByteArray();
                offer(responseArray);

            } catch (Exception e) {
                e.printStackTrace();
                log.warn("WDA客户端运行错误 {}", e.getMessage());
            }
        }
        IOUtils.closeQuietly(this.socket);
    }

    private byte[] subByteArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[0];
        try {
            byte2 = new byte[end - start];
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
        }
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
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
        IOSScreenServer3 screenServer3 = new IOSScreenServer3(9002);
        screenServer3.start();
    }

}
