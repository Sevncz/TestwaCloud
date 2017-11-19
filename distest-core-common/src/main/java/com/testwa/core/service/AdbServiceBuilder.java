package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.testwa.core.os.ExecutableFinder;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wen on 2016/12/13.
 */
public class AdbServiceBuilder extends CommonRunnerDriverService.Builder<AdbDriverService, AdbServiceBuilder>  {
    private static final String ADB = "adb";
    private static final String ANDROID_HOME = "ANDROID_HOME";

    @Override
    protected String findDefaultExecutable() {
        String androidHome = System.getenv(ANDROID_HOME);
        if(StringUtils.isBlank(androidHome)){
            throw new NullPointerException("ANDROID_HOME was not found.");
        }
        String adb = Paths.get(androidHome, "platform-tools", ADB).toString();
        return checkNotNull(new ExecutableFinder().find(adb),
                "Unable to find executable for: %s", "adb");
    }

    @Override
    protected ImmutableList<String> createArgs() {
        return null;
    }

    @Override
    protected AdbDriverService createRunnerDriverService(String exe, ImmutableList<String> args, ImmutableMap<String, String> environment) {
        return null;
    }
}
