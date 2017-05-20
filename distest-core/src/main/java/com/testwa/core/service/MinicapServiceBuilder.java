package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wen on 16/8/28.
 */
public final class MinicapServiceBuilder extends AdbServiceBuilder {
    private String getAdbExecutable;
    private long startupTimeout = 120L;
    private TimeUnit timeUnit;
    private String libPath;
    private String size;
    private String bin;
    private String deviceId;

    public MinicapServiceBuilder() {
        this.timeUnit = TimeUnit.SECONDS;
    }

    @Override
    protected ImmutableList<String> createArgs() {
        List<String> argList = new ArrayList<>();
        argList.add("-s");
        argList.add(this.deviceId);
        argList.add("shell");
        argList.add(String.format("LD_LIBRARY_PATH=%s", this.libPath));
        argList.add(String.format("%s/%s", this.libPath, this.bin));
        argList.add("-P");
        argList.add(String.format("%s@%s/0", this.size, this.size));
        return (new ImmutableList.Builder()).addAll(argList).build();
    }


    @Override
    protected AdbDriverService createRunnerDriverService(String executable, ImmutableList<String> args, ImmutableMap<String, String> environment) {
        try {
            return new AdbDriverService(executable, args, environment, this.startupTimeout, this.timeUnit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public MinicapServiceBuilder whithLibPath(String libPath) {
        this.libPath = libPath;
        return this;
    }

    public MinicapServiceBuilder whithSize(String size) {
        this.size = size;
        return this;
    }

    public MinicapServiceBuilder whithBin(String bin) {
        this.bin = bin;
        return this;
    }

    public MinicapServiceBuilder whithDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

}
