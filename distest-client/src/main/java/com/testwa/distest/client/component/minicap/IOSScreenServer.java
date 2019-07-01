package com.testwa.distest.client.component.minicap;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
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
public class IOSScreenServer extends Thread implements Closeable, ScreenSubject {
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE = "Content-type: image/jpeg";
    private BlockingQueue<byte[]> frameQueue;
    /** 队列大小 */
    private int queueSize = 50;
    private URL url;
    private InputStream urlStream;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private List<ScreenProjectionObserver> observers = new ArrayList<>();


    public IOSScreenServer(int port) {
        super("wda-client");
        try {
            this.url = new URL("Http://127.0.0.1:" + port);
        } catch (MalformedURLException e) {
        }
        init();
    }

    /**
     * 获取帧，如果没有则阻塞
     * @return 帧
     * @throws InterruptedException 阻塞中断
     */
    public synchronized byte[] take() throws InterruptedException {
        checkClosed();
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
        try {
            if(urlStream != null) {
                urlStream.close();
            }
        } catch (IOException e) {

        }
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
        while (isRunning.get()) {
            try {
                try {
                    URLConnection urlConn = url.openConnection();
                    urlConn.setReadTimeout(5000);
                    urlConn.connect();
                    urlStream = urlConn.getInputStream();
                } catch (IOException e) {
                    log.warn("[WDA 客户端] 连接失败，即将重试 {}", url.toString());
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e1) {

                    }
                    continue;
                }
                byte[] imageBytes = retrieveNextImage();
                log.debug("[WDA 客户端] get one frame");
                offer(imageBytes);
                try {
                    if(urlStream != null) {
                        urlStream.close();
                    }
                } catch (IOException e) {

                }
            } catch (Exception e) {
                log.error("[WDA 客户端] 解析异常", e);
            }

        }
        log.info("[WDA 客户端]采集屏幕 已退出");
    }

    /**
     * Using the urlStream get the next JPEG image as a byte[]
     *
     * @return byte[] of the JPEG
     * @throws IOException
     */
    private byte[] retrieveNextImage() throws IOException {
        int currByte = -1;

        String header = null;
        // build headers
        // the DCS-930L stops it's headers

        boolean captureContentLength = false;
        StringWriter contentLengthStringWriter = new StringWriter(128);
        StringWriter headerWriter = new StringWriter(128);

        int contentLength = 0;

        while ((currByte = urlStream.read()) > -1) {
            if (captureContentLength) {
                if (currByte == 10 || currByte == 13) {
                    contentLength = Integer.parseInt(contentLengthStringWriter.toString());
                    break;
                }
                contentLengthStringWriter.write(currByte);

            } else {
                headerWriter.write(currByte);
                String tempString = headerWriter.toString();
                int indexOf = tempString.indexOf(CONTENT_LENGTH);
                if (indexOf > 0) {
                    captureContentLength = true;
                }
            }
        }

        // 255 indicates the start of the jpeg image
        while ((urlStream.read()) != 255) {
            // just skip extras
        }

        // rest is the buffer
        byte[] imageBytes = new byte[contentLength + 1];
        // since we ate the original 255 , shove it back in
        imageBytes[0] = (byte) 255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }

        return imageBytes;
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
        IOSScreenServer screenServer = new IOSScreenServer(9002);
        screenServer.start();
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
