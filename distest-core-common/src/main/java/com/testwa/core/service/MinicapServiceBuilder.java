package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
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
    private Integer rotate;
    private String bin;
    private String deviceId;
    private String name;
    private Boolean shipFrame;
    private String[] args;

    public MinicapServiceBuilder() {
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
        argList.add(String.format("LD_LIBRARY_PATH=%s", this.libPath));
        argList.add(String.format("%s/%s", this.libPath, this.bin));
        argList.add("-P");
        argList.add(this.size);
        if(this.name != null){
            argList.add("-n");
            argList.add(this.name);
        }
        if (this.shipFrame)
            argList.add("-S");
        if (this.args != null) {
            for (String s : args) {
                argList.add(s);
            }
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

    public MinicapServiceBuilder whithRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

    public MinicapServiceBuilder whithShipFrame(Boolean shipFrame) {
        this.shipFrame = shipFrame;
        return this;
    }

    public MinicapServiceBuilder whithArgs(String[] args) {
        this.args = args;
        return this;
    }
    public MinicapServiceBuilder whithName(String name) {
        this.name = name;
        return this;
    }

}
