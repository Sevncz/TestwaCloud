package com.testwa.distest.client.component.minicap;import com.testwa.distest.client.android.ADBCommandUtils;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import org.apache.commons.lang3.StringUtils;import java.io.Closeable;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Paths;import java.util.concurrent.TimeUnit;import java.util.concurrent.TimeoutException;@Slf4jpublic class ScreenAndroidProjection extends Thread implements Closeable {    /** 缩放 */    private float zoom = 1;    /** 旋转 0|90|180|270 */    private int rotate = 0;    /** -Q <value>: JPEG quality (0-100) */    private int quality = 100;    /** 是否记录视频，true: 只记录视频 false: 只上传*/    private boolean isRecording = false;    /** 是否记录视频，true: 只记录视频 false: 只上传*/    private String outputFile = "./record.flv";    private String deviceId;    private MinicapAndroidServer server;    private MinicapClient client;    private VideoRecorder recorder;    private ScreenListener listener;    public ScreenAndroidProjection(String deviceId, ScreenListener listener) {        this.deviceId = deviceId;        this.listener = listener;    }    /**     * 设置缩放比例，默认为1不缩放     * @param zoom 缩放比例     */    public void setZoom(float zoom) {        this.zoom = zoom;    }    /**     * 设置旋转角度，默认为0不旋转     * @param rotate 旋转角度     */    public void setRotate(int rotate) {        this.rotate = rotate;    }    /**     * 设置图片质量，默认为100最高质量     * @param quality 图片质量     */    public void setQuality(int quality) {        this.quality = quality;    }    public void initRecorder(String outputFile) {        if(StringUtils.isBlank(outputFile)) {            throw new IllegalArgumentException("outputFile is null");        }        this.isRecording = true;        this.outputFile = outputFile;    }    /**     * 重启     */    public void restart() {        this.server.setZoom(zoom);        this.server.setRotate(rotate);        this.server.setQuality(quality);        this.server.restart();    }    private void startServer() {        this.server = new MinicapAndroidServer(deviceId);        this.server.setZoom(zoom);        this.server.setRotate(rotate);        this.server.setQuality(quality);        this.server.start();    }    private void startClient() {        int port = this.server.getPort();        if(isRecording) {            startVidoRecorder();            this.client = new MinicapClient(port, this.recorder);        }else{            this.client = new MinicapClient(port);        }        while (!this.server.isReady()){            try {                TimeUnit.SECONDS.sleep(1);            } catch (InterruptedException e) {            }        }        this.client.start();    }    private void startVidoRecorder() {        try {            this.recorder = new VideoRecorder();            this.recorder.setSize(ADBCommandUtils.getPhysicalSize(deviceId));            this.recorder.setOutputFile(outputFile);            this.recorder.setZoom(zoom);            this.recorder.setH264();            this.recorder.start();        } catch (Exception e) {            e.printStackTrace();        }    }    public boolean isRunning() {        if(this.server == null) {            return false;        }        if(this.client == null) {            return false;        }        return this.server.isRunning() && this.client.isRunning();    }    @Override    public void run() {        try {            startServer();            startClient();            while (this.server.isRunning()) {                byte[] take = this.client.take();                try {                    this.listener.projection(take);                } catch (Exception e) {                    e.printStackTrace();                }                long start = System.currentTimeMillis();                while(true) {                    long end = System.currentTimeMillis();                    if((end - start) >= 100) {                        break;                    }                    this.client.take();                }            }        } catch (Exception e) {            log.warn("屏幕映射失败 {}", e.getMessage());        } finally {            close();        }    }    @Override    public void close() {        try {            if(recorder != null) {                recorder.close();            }        } catch (IOException e) {            e.printStackTrace();        }        IOUtils.closeQuietly(client);        IOUtils.closeQuietly(server);        this.interrupt();    }    public byte[] frame() {        byte[] frame = null;        try {            frame = this.client.take();        } catch (InterruptedException e) {            e.printStackTrace();        }        return frame;    }}