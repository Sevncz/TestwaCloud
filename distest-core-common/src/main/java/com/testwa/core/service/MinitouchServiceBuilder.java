package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wen on 16/8/28.
 */
public final class MinitouchServiceBuilder extends AdbServiceBuilder {
    private String getAdbExecutable;
    private long startupTimeout = 120L;
    private TimeUnit timeUnit;
    private String libPath;
    private String size;
    private Integer rotate;
    private String execute;
    private String deviceId;
    private String name;
    private Boolean shipFrame;
    private String[] args;

    public MinitouchServiceBuilder() {
        this.timeUnit = TimeUnit.SECONDS;
    }


    /*
    Usage: /data/local/tmp/minicap [-h] [-n <name>]
      -d <id>:       Display ID. (0)
      -n <name>:     Change the name of the abtract unix domain socket. (minicap)
      -P <value>:    Display projection (<w>x<h>@<w>x<h>/{0|90|180|270}).
      -Q <value>:    JPEG quality (0-100).
      -s:            Take a screenshot and output it to stdout. Needs -P.
      -S:            Skip frames when they cannot be consumed quickly enough.
      -t:            Attempt to get the capture method running, then exit.
      -i:            Get display information in JSON format. May segfault.
     */
    @Override
    protected ImmutableList<String> createArgs() {
        List<String> argList = new ArrayList<>();
        argList.add("-s");
        argList.add(this.deviceId);
        argList.add("shell");
        argList.add(this.execute);
        argList.add("-n");
        argList.add(this.name);

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


    public MinitouchServiceBuilder whithExecute(String execute) {
        this.execute = execute;
        return this;
    }

    public MinitouchServiceBuilder whithName(String name) {
        this.name = name;
        return this;
    }

    public MinitouchServiceBuilder whithDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }


}
