package com.testwa.core.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 执行adb命令
 * Created by wen on 16/8/28.
 */
public class AdbDriverService extends CommonRunnerDriverService {

    private final File adbExec;
    private final ImmutableList<String> adbArgs;
    private final ImmutableMap<String, String> androidEnvironment;
    private final long startupTimeout;
    private final TimeUnit timeUnit;
    private final ReentrantLock lock = new ReentrantLock(true);
    private CommandLine process;
    private final ListOutputStream stream;

    AdbDriverService(File adbExec, ImmutableList<String> adbArgs, ImmutableMap<String, String> androidEnvironment, long startupTimeout, TimeUnit timeUnit) throws IOException {
        super(adbExec, adbArgs, androidEnvironment);
        this.process = null;
        this.adbExec = adbExec;
        this.adbArgs = adbArgs;
        this.androidEnvironment = androidEnvironment;
        this.startupTimeout = startupTimeout;
        this.timeUnit = timeUnit;
        this.stream =  (new ListOutputStream()).add(System.out);
    }

    public static AdbDriverService buildDefaultService() {
        return buildService(new AdbServiceBuilder());
    }

    public static AdbDriverService buildService(AdbServiceBuilder builder) {
        return builder.build();
    }

    public Boolean isSuccess() {
        if(this.isRunning()) {
            return null;
        }
        return this.process.isSuccessful();
    }


    public void start() throws AdbProcessHasNotBeenStartedLocallyException {
        this.lock.lock();

        try {
            if(!this.isRunning()) {
                try {
                    this.process = new CommandLine(this.adbExec.getCanonicalPath(),
                            this.adbArgs.toArray(new String[0]));
                    this.process.setEnvironmentVariables(this.androidEnvironment);
                    this.process.copyOutputTo(this.stream);
                    this.process.executeAsync();
                    return;
                } catch (Throwable e) {
                    this.destroyProcess();
                    String msgTxt = "The adb has not been started. The given adb executable: " + this.adbExec + " Arguments: " + this.adbArgs.toString() + " " + "\n";
                    if(this.process != null) {
                        String processStream = this.process.getStdOut();
                        if(!StringUtils.isBlank(processStream)) {
                            msgTxt = msgTxt + "Process output: " + processStream + "\n";
                        }
                    }

                    throw new AdbProcessHasNotBeenStartedLocallyException(msgTxt, e);
                }
            }
        } finally {
            this.lock.unlock();
        }

    }

    public void stop() {
        this.lock.lock();

        try {
            if(this.process != null) {
                this.destroyProcess();
            }

            this.process = null;
        } finally {
            this.lock.unlock();
        }

    }

    private void destroyProcess() {
        if(this.process.isRunning()) {
            this.process.destroy();
        }

    }

    public String getStdOut() {
        return this.process != null?this.process.getStdOut():null;
    }

    public void addOutPutStream(OutputStream outputStream) {
        Preconditions.checkNotNull(outputStream, "outputStream parameter is NULL!");
        this.stream.add(outputStream);
    }

    public void addOutPutStreams(List<OutputStream> outputStreams) {
        Preconditions.checkNotNull(outputStreams, "outputStreams parameter is NULL!");
        Iterator its = outputStreams.iterator();

        while(its.hasNext()) {
            OutputStream stream = (OutputStream)its.next();
            this.addOutPutStream(stream);
        }

    }

}
