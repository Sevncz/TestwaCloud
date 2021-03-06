package com.testwa.distest.client.device.listener;

import com.testwa.distest.client.component.executor.task.TestTaskListener;
import com.testwa.distest.client.component.logcat.LogListener;
import com.testwa.distest.client.component.minicap.ScreenListener;
import com.testwa.distest.client.component.minicap.ScreenProjectionObserver;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.util.ImgCompress;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.ScreenCaptureRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wen
 * @create 2019-05-23 20:33
 */
@Slf4j
public class IOSComponentServiceRunningListener implements ScreenProjectionObserver, LogListener, TestTaskListener {

    private final String deviceId;
    private final DeivceRemoteApiClient api;
    private boolean isLogWaitting = true;
    // 帧率
    private int framerate = 30;
    private long latesenttime = 0;
    private double defaultScale = 0.3;

    private final static String adb_log_line_regex = "(.\\S*) *(.\\S*) *(\\d*) *(\\d*) *([A-Z]) *([^:]*): *(.*?)$";

    public IOSComponentServiceRunningListener(String deviceId, DeivceRemoteApiClient api) {
        this.deviceId = deviceId;
        this.api = api;
    }

    @Override
    public void taskFinish() {

    }

    @Override
    public byte[] takeFrame() {
        return new byte[0];
    }

    @Override
    public String getVideoFile() {
        return null;
    }

    @Override
    public void onLog(byte[] bytes) {
        if (!isLogWaitting) {
            String content = new String(bytes);
            api.sendLog(content, this.deviceId);
        }
    }

    /**
     * @Description:
     * if (latesenttime == 0 || Date.now()-latesenttime > 1000/framerate) {
     *     latesenttime = Date.now()
     *     return send(frame, {
     *       binary: true
     *     })
     *   }
     * @Param: [frame]
     * @Return: void
     * @Author wen
     * @Date 2019/6/11 17:13
     */
    @Override
    public void frameImageChange(byte[] image) {
        if (latesenttime == 0 || System.currentTimeMillis() - latesenttime > 1000/framerate) {
            this.latesenttime = System.currentTimeMillis();
            byte[] scaleByte = ImgCompress.decompressPicByte(image, defaultScale);
            api.getScreenStub().onNext(api.getScreenCaptureRequest(scaleByte, this.deviceId));
            log.debug("[upload frame]");
        }
    }

    public void rate(Integer rate) {
        if(rate > 2 && rate < 100) {
            this.framerate = rate;
        }
    }

    public void setLogWait(boolean isWaitting) {
        this.isLogWaitting = isWaitting;
    }
}
