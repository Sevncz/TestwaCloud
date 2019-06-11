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
import io.rpc.testwa.device.LogcatMessageRequest;
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
public class AndroidComponentServiceRunningListener implements ScreenListener, LogListener, TestTaskListener {

    private final String deviceId;
    private final DeivceRemoteApiClient api;
    private boolean isScreenWaitting = true;
    private boolean isLogWaitting = true;
    // 帧率
    private int framerate = 20;
    private long latesenttime = 0;

    /**
     * 在Android的ADB的情况下，我们是使用adb logcat -v brief -v threadtime
     */
    private final static String adb_log_line_regex = "(.\\S*) *(.\\S*) *(\\d*) *(\\d*) *([A-Z]) *([^:]*): *(.*?)$";
    private Pattern logAndroidPattern;
    private LogCatFilter logCatFilter;


    public AndroidComponentServiceRunningListener(String deviceId, DeivceRemoteApiClient api) {
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
        List<LogcatMessageRequest> logcatMessage = getAndroidLogMessageRequests(bytes);
        api.sendLogcatMessages(logcatMessage, this.deviceId);
    }

    @Override
    public void projection(byte[] frame) {
        if (!isScreenWaitting) {
            if (latesenttime == 0 || System.currentTimeMillis() - latesenttime > 1000/framerate) {
                this.latesenttime = System.currentTimeMillis();
                api.saveScreen(frame, this.deviceId);
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

    private List<LogcatMessageRequest> getAndroidLogMessageRequests(byte[] bytes) {
        List<LogcatMessageRequest> logLines = new ArrayList<>();
        String logstr = new String(bytes, StandardCharsets.UTF_8).replace("\0", "");

        String[] lines = logstr.split("\n");
        for(String line : lines) {
            // 正则解析 logstr
            Matcher matcher = logAndroidPattern.matcher(line);
            if(matcher.matches()){
                String logData = matcher.group(1);
                String logTime = matcher.group(2);
                String logProcess = matcher.group(3);
                String logThread = matcher.group(4);
                Log.LogLevel logLevel = Log.LogLevel.getByLetterString(matcher.group(5));
                if(logLevel == null) {
                    continue;
                }
                String logTag = matcher.group(6);
                String logMessage = matcher.group(7);
                LogCatMessage logCatMessage = new LogCatMessage(logData, logTime, logProcess, logThread, logLevel, logTag, logMessage);
                if(logCatFilter.matches(logCatMessage)) {
                    LogcatMessageRequest messageRequest = LogcatMessageRequest.newBuilder()
                            .setData(logData)
                            .setTime(logTime)
                            .setPid(logProcess)
                            .setThread(logThread)
                            .setLevel(logLevel.getStringValue())
                            .setTag(logTag)
                            .setMessage(logMessage)
                            .build();
                    logLines.add(messageRequest);
                }
            }
        }
        return logLines;
    }

    public void buildLogcatFilter(String command) {
        if(StringUtils.isNotEmpty(command)) {
            Map filterParams = JSON.parseObject(command, Map.class);
            if(filterParams != null) {
                String tag = (String) filterParams.getOrDefault("tag", "");
                String pid = (String) filterParams.getOrDefault("pid", "");
                Log.LogLevel level = Log.LogLevel.getByLetterString((String) filterParams.getOrDefault("level", "E"));
                String message = (String) filterParams.get("message");
                this.logCatFilter = new LogCatFilter(tag, message, pid, level);
            }
        }
    }
}
