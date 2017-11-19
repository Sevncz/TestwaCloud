package com.testwa.core.os;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

interface OsProcess {
    Map<String, String> getEnvironment();

    void setEnvironmentVariable(String name, String value);

    void copyOutputTo(OutputStream out);

    void setInput(String allInput);

    void setWorkingDirectory(File workingDirectory);

    void executeAsync();

    void waitFor() throws InterruptedException;

    void waitFor(long timeout) throws InterruptedException;

    int destroy();

    int getExitCode();

    String getStdOut();

    boolean isRunning();

    void checkForError();
}