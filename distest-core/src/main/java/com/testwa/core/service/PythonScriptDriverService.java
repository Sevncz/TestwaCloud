package com.testwa.core.service;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.testwa.core.os.CommandLine;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 执行python命令
 * Created by wen on 16/8/28.
 */
public final class PythonScriptDriverService extends CommonRunnerDriverService {
    private final String pythonExec;
    private final ImmutableList<String> pythonArgs;
    private final ImmutableMap<String, String> pythonEnvironment;
    private final long startupTimeout;
    private final TimeUnit timeUnit;
    private final ReentrantLock lock = new ReentrantLock(true);
    private CommandLine process;
    private final ListOutputStream stream;

    PythonScriptDriverService(String pythonExec, ImmutableList<String> pythonArgs, ImmutableMap<String, String> pythonEnvironment, long startupTimeout, TimeUnit timeUnit) throws IOException {
        super(pythonExec, pythonArgs, pythonEnvironment);
        this.process = null;
        this.pythonExec = pythonExec;
        this.pythonArgs = pythonArgs;
        this.pythonEnvironment = pythonEnvironment;
        this.startupTimeout = startupTimeout;
        this.timeUnit = timeUnit;
        this.stream =  (new ListOutputStream()).add(System.out);
    }

    public static PythonScriptDriverService buildDefaultService() {
        return buildService(new PythonServiceBuilder());
    }

    public static PythonScriptDriverService buildService(PythonServiceBuilder builder) {
        return builder.build();
    }

    public boolean isRunning() {
        this.lock.lock();

        try {
            if(this.process == null) {
                return false;
            }

            if(!this.process.isRunning()) {
                return false;
            }

            return true;
        } finally {
            this.lock.unlock();
        }
    }


    public Boolean isSuccess() {
        if(this.isRunning()) {
            return null;
        }
        return this.process.isSuccessful();
    }


    public void start() throws AppiumServerHasNotBeenStartedLocallyException {
        this.lock.lock();

        try {
            if(!this.isRunning()) {
                try {
                    this.process = new CommandLine(this.pythonExec, (String[])this.pythonArgs.toArray(new String[0]));
                    this.process.setEnvironmentVariables(this.pythonEnvironment);
                    this.process.copyOutputTo(this.stream);
                    this.process.executeAsync();
                    return;
                } catch (Throwable e) {
                    this.destroyProcess();
                    String msgTxt = "The python script has not been started. The given python executable: " + this.pythonExec + " Arguments: " + this.pythonArgs.toString() + " " + "\n";
                    if(this.process != null) {
                        String processStream = this.process.getStdOut();
                        if(!StringUtils.isBlank(processStream)) {
                            msgTxt = msgTxt + "Process output: " + processStream + "\n";
                        }
                    }

                    throw new AppiumServerHasNotBeenStartedLocallyException(msgTxt, e);
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
        Iterator var3 = outputStreams.iterator();

        while(var3.hasNext()) {
            OutputStream stream = (OutputStream)var3.next();
            this.addOutPutStream(stream);
        }

    }

}
