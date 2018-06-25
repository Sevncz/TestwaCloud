package com.testwa.distest.client.component.minicap;import com.testwa.core.utils.Common;import com.testwa.distest.client.component.minicap.Banner;import lombok.extern.slf4j.Slf4j;import java.io.DataInputStream;import java.io.IOException;import java.io.InputStream;import java.net.Socket;import java.util.ArrayList;import java.util.Arrays;import java.util.List;import java.util.concurrent.*;/** * @Program: distest * @Description: 获得 minicap 的数据并发送 * @Author: wen * @Create: 2018-06-20 11:58 **/@Slf4jpublic class MinicapImgSender {    private ScheduledExecutorService service = null;    private BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();    private Banner banner = new Banner();    private int port;    private Socket socket;    private int readBannerBytes = 0;    private int bannerLength = 2;    private int readFrameBytes = 0;    private int frameBodyLength = 0;    private byte[] frameBody = new byte[0];    private int total;    private int initialDelay = 0;    private int delay = 2000;    private boolean isRunning;    // listener    private List<MinicapListener> listenerList = new ArrayList<MinicapListener>();    public MinicapImgSender(int port) {        this.port = port;    }    public void start() {        this.isRunning = true;        service = Executors.newScheduledThreadPool(2);        service.scheduleWithFixedDelay(startInitial, initialDelay, delay, TimeUnit.MILLISECONDS);//        service.scheduleWithFixedDelay(imgParserRunnable, initialDelay,delay, TimeUnit.MILLISECONDS);    }    public void stop() {        this.isRunning = false;        service.shutdown();    }    private Runnable startInitial = () -> {        InputStream stream = null;        DataInputStream input = null;        try {            socket = new Socket("127.0.0.1", this.port);            // 连接minicap启动的服务            while (isRunning) {                byte[] buffer;                int len = 0;                while (len == 0) {                    stream = socket.getInputStream();                    input = new DataInputStream(stream);                    len = input.available();                }                buffer = new byte[len];                input.read(buffer);                log.debug("length={}; len={}", buffer.length, len);                if(buffer.length == 4){                    log.debug("content=" + buffer[0]);                }                // bytes内包含有信息，需要给Dataparser处理//                dataQueue.add(Arrays.copyOfRange(buffer, 0, buffer.length));                byte[] currentBuffer = Arrays.copyOfRange(buffer, 0, buffer.length);                banner = new Banner();                readData(currentBuffer, len);                log.debug(banner.toString());            }        } catch (Exception e) {            log.error("img sender error {}", this.port, e);        } finally {            log.info("ios Minicap img sender end, isrunning {}", isRunning);            if (socket != null && socket.isConnected()) {                try {                    socket.close();                } catch (IOException e) {                    // TODO Auto-generated catch block                    e.printStackTrace();                }            }            if (stream != null) {                try {                    stream.close();                } catch (IOException e) {                    // TODO Auto-generated catch block                    e.printStackTrace();                }            }        }    };//    private final Runnable imgParserRunnable = new Runnable() {////        @Override//        public void run() {//            while (isRunning) {//                try {//                } catch (Exception e) {//                    log.error("readData error", e);//                }//            }//        }////    };    void readData(byte[] buffer, int length) {        for (int cursor = 0; cursor < length; ) {            int byte10 = buffer[cursor] & 0xff;            if (readBannerBytes < bannerLength) {                cursor = parserBanner(cursor, byte10);            } else if (readFrameBytes < 4) { // frame length                frameBodyLength += (byte10 << (readFrameBytes * 8));                cursor += 1;                readFrameBytes += 1;                total = frameBodyLength;            } else {                log.debug(String.format("图片的大小 : %d KB", total/1024));                if (length - cursor >= frameBodyLength) {                    byte[] subByte = Arrays.copyOfRange(buffer, cursor, cursor + frameBodyLength);                    frameBody = Common.mergeArray(frameBody, subByte);                    if ((frameBody[0] != -1) || frameBody[1] != -40) {                        log.error("Frame body does not with JPG header");                        return;                    }                    log.debug(String.format("实际图片的大小 : %d KB", frameBody.length/1024));                    byte[] finalBytes = Arrays.copyOfRange(frameBody, 0, frameBody.length);                    onJPG(finalBytes);                    cursor += frameBodyLength;                    frameBodyLength = 0;                    readFrameBytes = 0;                    frameBody = new byte[0];                } else {                    byte[] subByte = Arrays.copyOfRange(buffer, cursor, length);                    frameBody = Common.mergeArray(frameBody, subByte);                    frameBodyLength -= (length - cursor);                    readFrameBytes += (length - cursor);                    cursor = length;                }            }        }    }    ////// banner    int pid = 0;    int realWidth = 0;    int realHeight = 0;    int virtualWidth = 0;    int virtualHeight = 0;    int orientation = 0;    int quirks = 0;    int parserBanner(int cursor, int ch) {        switch (cursor) {            case 0:                banner.setVersion(ch);                break;            case 1:                bannerLength = ch;                banner.setLength(bannerLength);                break;            case 2:            case 3:            case 4:            case 5: {                pid += (ch << ((readBannerBytes - 2) * 8));                banner.setPid(pid);                break;            }            case 6:            case 7:            case 8:            case 9: {                realWidth += (ch << ((readBannerBytes - 6) * 8));                banner.setReadWidth(realWidth);                break;            }            case 10:            case 11:            case 12:            case 13:                realHeight += (ch << ((readBannerBytes - 10) * 8));                banner.setReadHeight(realHeight);                break;            case 14:            case 15:            case 16:            case 17:                virtualWidth += (ch << ((readBannerBytes - 14) * 8));                banner.setVirtualWidth(virtualWidth);                break;            case 18:            case 19:            case 20:            case 21:                virtualHeight += (ch << ((readBannerBytes - 18) * 8));                banner.setVirtualHeight(virtualHeight);                break;            case 22:                banner.setOrientation(ch * 90);                break;            case 23:                banner.setQuirks(ch);                break;        }        cursor += 1;        readBannerBytes += 1;        return cursor;    }    public void addEventListener(MinicapListener listener) {        if (listener != null) {            this.listenerList.add(listener);        }    }    private void onJPG(byte[] data) {        for (MinicapListener listener : listenerList) {            listener.onJPG(data);        }    }//    public void restart() {//        stop();//        start();//    }    public static void main(String[] args) {        MinicapImgSender s = new MinicapImgSender(7501);        s.start();    }}