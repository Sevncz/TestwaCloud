package com.testwa.distest.client.device.listener;

import com.alibaba.fastjson.JSON;
import com.android.ddmlib.Log;
import com.testwa.distest.client.component.executor.task.TestTaskListener;
import com.testwa.distest.client.component.logcat.LogCatFilter;
import com.testwa.distest.client.component.logcat.LogCatMessage;
import com.testwa.distest.client.component.logcat.LogListener;
import com.testwa.distest.client.component.minicap.ScreenListener;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.util.ImgCompress;
import io.rpc.testwa.device.LogRequest;
import io.rpc.testwa.device.LogcatMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wen
 * @create 2019-05-23 20:33
 */
@Slf4j
public class IOSComponentServiceRunningListener implements ScreenListener, LogListener, TestTaskListener {

    private final String deviceId;
    private final DeivceRemoteApiClient api;
    private boolean isScreenWaitting = true;
    private boolean isLogWaitting = true;
    // 帧率
    private int framerate = 20;
    private long latesenttime = 0;

    private final static String adb_log_line_regex = "(.\\S*) *(.\\S*) *(\\d*) *(\\d*) *([A-Z]) *([^:]*): *(.*?)$";
    private Pattern logAndroidPattern;
    private LogCatFilter logCatFilter;


    public IOSComponentServiceRunningListener(String deviceId, DeivceRemoteApiClient api) {
        this.deviceId = deviceId;
        this.api = api;
        this.logAndroidPattern = Pattern.compile(adb_log_line_regex);
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
    public void projection(byte[] frame) {
        if (!isScreenWaitting) {
            if (latesenttime == 0 || System.currentTimeMillis() - latesenttime > 1000/framerate) {
                this.latesenttime = System.currentTimeMillis();
                byte[] scaleFrame = ImgCompress.decompressPicByte(frame, 0.2f);
                api.saveScreen(scaleFrame, this.deviceId);
            }
        }
    }

    public void rate(Integer rate) {
        if(rate > 2 && rate < 100) {
            this.framerate = rate;
        }
    }

    public void setScreenWait(boolean isWaitting) {
        this.isScreenWaitting = isWaitting;
    }

    public void setLogWait(boolean isWaitting) {
        this.isLogWaitting = isWaitting;
    }

}
