package com.testwa.core.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.testwa.core.os.CommandLine;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wen on 16/8/28.
 */
public class CommonRunnerDriverService {

    private final ReentrantLock lock = new ReentrantLock();
    private CommandLine process = null;
    private final String executable;
    private final ImmutableList<String> args;
    private final ImmutableMap<String, String> environment;

    protected CommonRunnerDriverService(String executable, ImmutableList<String> args, ImmutableMap<String, String> environment) throws IOException {
        this.executable = executable;
        this.args = args;
        this.environment = environment;
    }

    protected static File findExecutable(String exeName, String exeProperty, String exeDocs, String exeDownload) {
        String defaultPath = CommandLine.find(exeName);
        String exePath = System.getProperty(exeProperty, defaultPath);
        Preconditions.checkState(exePath != null, "The path to the driver executable must be set by the %s system property; for more information, see %s. The latest version can be downloaded from %s", new Object[]{exeProperty, exeDocs, exeDownload});
        File exe = new File(exePath);
        checkExecutable(exe);
        return exe;
    }

    protected static void checkExecutable(File exe) {
        Preconditions.checkState(exe.exists(), "The driver executable does not exist: %s", new Object[]{exe.getAbsolutePath()});
        Preconditions.checkState(!exe.isDirectory(), "The driver executable is a directory: %s", new Object[]{exe.getAbsolutePath()});
        Preconditions.checkState(FileHandler.canExecute(exe).booleanValue(), "The driver is not executable: %s", new Object[]{exe.getAbsolutePath()});
    }

    public boolean isRunning() {
        this.lock.lock();

        boolean e;
        try {
            if(this.process != null) {
                e = this.process.isRunning();
                return e;
            }

            e = false;
        } catch (IllegalThreadStateException var6) {
            boolean var2 = true;
            return var2;
        } finally {
            this.lock.unlock();
        }

        return e;
    }

    public void start() throws IOException {
        this.lock.lock();

        try {
            if(this.process == null) {
                this.process = new CommandLine(this.executable, (String[])this.args.toArray(new String[0]));
                this.process.setEnvironmentVariables(this.environment);
                this.process.copyOutputTo(System.err);
                this.process.executeAsync();
                return;
            }
        } finally {
            this.lock.unlock();
        }

    }

    public void stop() {
        this.lock.lock();
        if(this.process != null) {
            this.process.destroy();
            return;
        }
        this.process = null;
        this.lock.unlock();
    }

    public abstract static class Builder<DS extends CommonRunnerDriverService, B extends Builder> {
        private String exe = null;
        private ImmutableMap<String, String> environment = ImmutableMap.of();
        private File logFile;

        public Builder() {
        }

        public B usingDriverExecutable(File file) {
            Preconditions.checkNotNull(file);
            CommonRunnerDriverService.checkExecutable(file);
            this.exe = file.getAbsolutePath();
            return (B)this;
        }

        public B withLogFile(File logFile) {
            this.logFile = logFile;
            return (B)this;
        }

        protected File getLogFile() {
            return this.logFile;
        }

        public DS build() {
            if(this.exe == null) {
                this.exe = this.findDefaultExecutable();
            }

            ImmutableList args = this.createArgs();
            return (DS)this.createRunnerDriverService(this.exe, args, this.environment);
        }

        protected abstract String findDefaultExecutable();

        protected abstract ImmutableList<String> createArgs();

        protected abstract DS createRunnerDriverService(String exe, ImmutableList<String> args, ImmutableMap<String, String> environment);
    }

}
