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
    private String getAdbExecutable;
    private long startupTimeout = 120L;
    private TimeUnit timeUnit;
    private String deviceId;
    private String filter;
    private String tag;
    private String keyword;
    private String level;
    private String format;
    private String buffer;
    private String grep;

    @Override
    protected ImmutableList<String> createArgs() {
        List<String> argList = new ArrayList<>();
        argList.add("-s");
        argList.add(this.deviceId);
        argList.add("logcat");
        if(StringUtils.isNotBlank(this.format)){
            argList.add("-v");
            argList.add(this.format);
        }
        if(StringUtils.isNotBlank(this.buffer)){
            argList.add("-b");
            argList.add(this.buffer);
        }
        if(StringUtils.isNotBlank(this.level)){
            if(StringUtils.isBlank(this.tag)){
                this.tag = "*";
            }
            argList.add(String.format("%s:%s", this.tag, this.level));
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

    public LogcatServiceBuilder whithDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public LogcatServiceBuilder whithTag(String tag) {
        this.tag = tag;
        return this;
    }

    public LogcatServiceBuilder whithKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public LogcatServiceBuilder whithFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public LogcatServiceBuilder whithLevel(String level) {
        this.level = level;
        return this;
    }

    public LogcatServiceBuilder whithBuffer(String buffer) {
        this.buffer = buffer;
        return this;
    }

    public LogcatServiceBuilder whithGrep(String grep) {
        this.grep = grep;
        return this;
    }

}
