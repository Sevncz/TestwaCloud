package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wen on 16/8/28.
 */
public final class LogcatServiceBuilder extends AdbServiceBuilder {
    private TimeUnit timeUnit;
    private String getAdbExecutable;
    private long startupTimeout = 120L;
    private String deviceId;
    private String content;

    @Override
    protected ImmutableList<String> createArgs() {
        List<String> argList = new ArrayList<>();
        argList.add("-s");
        argList.add(this.deviceId);
        argList.add("logcat");
        if(StringUtils.isNotBlank(this.content)){
            argList.add(this.content);
        }
        return (new ImmutableList.Builder()).addAll(argList).build();
    }


    @Override
    protected AdbDriverService createDriverService(File executable, int port, ImmutableList<String> immutableList, ImmutableMap<String, String> immutableMap) {
        try {
            return new AdbDriverService(executable, immutableList, immutableMap, this.startupTimeout, this.timeUnit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LogcatServiceBuilder withDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public LogcatServiceBuilder withContent(String content) {
        this.content = content;
        return this;
    }
    public LogcatServiceBuilder withClear() {
        this.content = "-c";
        return this;
    }

}
