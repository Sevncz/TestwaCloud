package com.testwa.distest.client.component.minicap;import com.testwa.core.utils.TimeUtil;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import org.apache.commons.lang3.ArrayUtils;import java.io.IOException;import java.io.InputStream;import java.net.Socket;import java.util.ArrayList;import java.util.List;import java.util.concurrent.BlockingQueue;import java.util.concurrent.LinkedBlockingQueue;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: 获得 minicap 的数据并发送 * @Author: wen * @Create: 2018-06-20 11:58 **/@Slf4jpublic class MinicapAndoridClient implements ScreenSubject{    private String host = "127.0.0.1";    private int port;    private Socket socket;    /** 队列大小 */    private int queueSize = 50;    /** 存放原始数据队列 */    private BlockingQueue<byte[]> dataQueue;    /** 存放帧队列 */    private BlockingQueue<byte[]> frameQueue;    /** Banner */    private Banner banner;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    private List<ScreenProjectionObserver> observers = new ArrayList<>();    public MinicapAndoridClient(int port) {        this.port = port;        this.dataQueue = new LinkedBlockingQueue<>(queueSize);        this.frameQueue = new LinkedBlockingQueue<>(queueSize);        this.banner = new Banner();    }    /**     * 获取帧，如果没有则阻塞     * @return 帧     * @throws InterruptedException 阻塞中断     */    public synchronized byte[] take() throws InterruptedException {        checkClosed();        if(frameQueue.isEmpty()){            return null;        }        return frameQueue.take();    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    public void close() {        this.isRunning.set(false);        IOUtils.closeQuietly(this.socket);    }    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("Minicap 客户端已运行");        } else {            this.isRunning.set(true);        }        Thread frame = new Thread(new ImageBinaryFrameCollector());        frame.start();        Thread converter = new Thread(new ImageConverter());        converter.start();    }    private byte[] subByteArray(byte[] byte1, int start, int end) {        byte[] byte2 = new byte[0];        try {            byte2 = new byte[end - start];        } catch (NegativeArraySizeException e) {            e.printStackTrace();        }        System.arraycopy(byte1, start, byte2, 0, end - start);        return byte2;    }    @Override    public void registerObserver(ScreenProjectionObserver o) {        observers.add(o);    }    @Override    public void removeObserver(ScreenProjectionObserver o) {        observers.remove(o);    }    @Override    public void notifyObservers(byte[] image) {        for(ScreenProjectionObserver observer : observers) {            observer.frameImageChange(image);        }    }    class ImageBinaryFrameCollector implements Runnable {        private InputStream inputStream = null;        @Override        public void run() {            log.debug("图片二进制数据收集器已经开启");            // 连接minicap服务            log.info("Minicap 客户端启动中 port: {}......", port);            boolean isConnected = false;            while (!isConnected){                try {                    socket = new Socket(host, port);                    socket.setSoTimeout(10*1000);                    isConnected = true;                } catch (IOException e) {                    log.error("与Minicap服务端连接失败，稍后再试......");                    try {                        TimeUnit.SECONDS.sleep(5);                    } catch (InterruptedException e1) {                        e1.printStackTrace();                    }                }            }            log.info("与Minicap服务端连接成功");            while (isRunning.get()) {                try {                    inputStream = socket.getInputStream();                    int len = 4096;                    byte[] buffer;                    buffer = new byte[len];                    int realLen = inputStream.read(buffer);                    if (buffer.length != realLen && realLen >= 0) {                        buffer = subByteArray(buffer, 0, realLen);                    }                    if(realLen >= 0) {                        dataQueue.add(buffer);                    }                } catch (Exception e) {//                log.warn("Minicap客户端运行错误 {}", e.getMessage());                }            }            IOUtils.closeQuietly(socket);            if(inputStream != null) {                try {                    inputStream.close();                } catch (IOException e) {                    // TODO Auto-generated catch block                    e.printStackTrace();                }            }            log.debug("图片二进制数据收集器已关闭");        }    }    class ImageConverter implements Runnable {        private int readBannerBytes = 0;        private int bannerLength = 2;//头的长度        private int readFrameBytes = 0;//已读byte长度        private int frameBodyLength = 0;//图片的byte长度        private byte[] frameBody = new byte[0];        @Override        public void run() {            // TODO Auto-generated method stub            long start = System.currentTimeMillis();            while (isRunning.get()) {                try {                    if (dataQueue.isEmpty()) {                        // log.info("数据队列为空");                        continue;                    }                    byte[] buffer = dataQueue.poll();                    int len = buffer.length;                    log.debug("长度："+len);                    for (int cursor = 0; cursor < len;) {                        int byte10 = buffer[cursor] & 0xff;                        if (readBannerBytes < bannerLength) {                            //第一次进来读取头部信息                            cursor = parserBanner(cursor, byte10);                        } else if (readFrameBytes < 4) {                            //读取并设置图片的大小                            // 第二次的缓冲区中前4位数字和为frame的缓冲区大小                            frameBodyLength += (byte10 << (readFrameBytes * 8));                            cursor += 1;                            readFrameBytes += 1;                            // log.debug("解析图片大小 = " + readFrameBytes);                        } else {                            log.debug("len:"+len+"cursor:"+cursor+"frameBodyLength:"+frameBodyLength);                            if (len - cursor >= frameBodyLength) {                                log.debug("frameBodyLength = " + frameBodyLength);                                byte[] subByte = ArrayUtils.subarray(buffer, cursor, cursor + frameBodyLength);                                frameBody = ArrayUtils.addAll(frameBody, subByte);                                if ((frameBody[0] != -1) || frameBody[1] != -40) {                                    log.error(String.format("Frame body does not start with JPG header"));                                    return;                                }                                log.debug("JPG头: "+frameBody[0]+","+frameBody[1]);                                offer(frameBody);                                long current = System.currentTimeMillis();                                log.debug("[{}] 图片已生成,耗时: "                                        + TimeUtil.formatElapsedTime(current                                        - start), port);                                start = current;                                cursor += frameBodyLength;                                restore();                            } else {                                log.debug("所需数据大小 : " + frameBodyLength);                                byte[] subByte = ArrayUtils.subarray(buffer, cursor, len);                                frameBody = ArrayUtils.addAll(frameBody, subByte);                                frameBodyLength -= (len - cursor);                                readFrameBytes += (len - cursor);                                cursor = len;                            }                        }                    }                }catch (Exception e){                }            }        }        private void restore() {            frameBodyLength = 0;            readFrameBytes = 0;            frameBody = new byte[0];        }        private int parserBanner(int cursor, int byte10) {            switch (readBannerBytes) {                case 0:                    // version                    banner.setVersion(byte10);                    break;                case 1:                    // length                    bannerLength = byte10;                    banner.setLength(byte10);                    break;                case 2:                case 3:                case 4:                case 5:                    // pid                    int pid = banner.getPid();                    pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;                    banner.setPid(pid);                    break;                case 6:                case 7:                case 8:                case 9:                    // real width                    int realWidth = banner.getRealWidth();                    log.info("realwidth0"+realWidth);                    realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;                    log.info("realwidth1"+realWidth);                    banner.setRealWidth(realWidth);                    break;                case 10:                case 11:                case 12:                case 13:                    // real height                    int realHeight = banner.getRealHeight();                    realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;                    banner.setRealHeight(realHeight);                    break;                case 14:                case 15:                case 16:                case 17:                    // virtual width                    int virtualWidth = banner.getVirtualWidth();                    virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;                    banner.setVirtualWidth(virtualWidth);                    log.debug("virtual"+virtualWidth);                    break;                case 18:                case 19:                case 20:                case 21:                    // virtual height                    int virtualHeight = banner.getVirtualHeight();                    virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;                    banner.setVirtualHeight(virtualHeight);                    log.debug("virtualhegith"+virtualHeight);                    break;                case 22:                    // orientation                    banner.setOrientation(byte10 * 90);                    break;                case 23:                    // quirks                    banner.setQuirks(byte10);                    break;            }            cursor += 1;            readBannerBytes += 1;            if (readBannerBytes == bannerLength) {                log.debug(banner.toString());            }            return cursor;        }    }    /**     * 将一帧放入到队列     */    protected void offer(byte[] frame) {        if (frameQueue.size() >= queueSize) {            frameQueue.poll();            log.debug("[{}]存放帧的队列已满，将会抛弃最旧的一帧", port);        }        long start = System.currentTimeMillis();        frameQueue.offer(frame);        long end = System.currentTimeMillis();        log.debug(String.valueOf(end -  start));    }    /**     * 检查是否关闭     */    protected void checkClosed() {        if (!this.isRunning.get()) {            throw new IllegalStateException("Minicap客户端已关闭");        }    }    public static void main(String[] args) {        MinicapAndoridClient s = new MinicapAndoridClient(7500);        s.start();//        MinicapAndoridClient s1 = new MinicapAndoridClient(7501);//        s1.start();    }}