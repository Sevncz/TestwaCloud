package com.testwa.core.service;

import com.github.cosysoft.device.shell.AndroidSdk;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;


/**
 * Created by wen on 2016/12/13.
 */
public class AdbServiceBuilder extends CommonRunnerDriverService.Builder<AdbDriverService, AdbServiceBuilder>  {

    @Override
    protected File findDefaultExecutable() {
        return AndroidSdk.adb();
    }

    @Override
    protected ImmutableList<String> createArgs() {
        return null;
    }

    @Override
    protected AdbDriverService createDriverService(File exe, int prot, ImmutableList<String> immutableList, ImmutableMap<String, String> immutableMap) {
        return null;
    }
}
