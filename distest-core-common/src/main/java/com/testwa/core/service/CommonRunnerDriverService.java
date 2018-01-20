package com.testwa.core.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wen on 16/8/28.
 */
public class CommonRunnerDriverService extends DriverService{

    protected CommonRunnerDriverService(File executable, ImmutableList<String> args, ImmutableMap<String, String> environment) throws IOException {
        super(executable, 0, args, environment);

    }


}
