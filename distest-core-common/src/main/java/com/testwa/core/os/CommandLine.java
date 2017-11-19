package com.testwa.core.os;

import com.google.common.annotations.VisibleForTesting;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

import static org.openqa.selenium.Platform.WINDOWS;
import static org.openqa.selenium.Platform.MAC;

public class CommandLine {

    private OsProcess process;

    public CommandLine(String executable, String... args) {
        process = new UnixProcess(executable, args);
    }

    public CommandLine(String[] cmdarray) {
        String executable = cmdarray[0];
        int length = cmdarray.length - 1;
        String[] args = new String[length];
        System.arraycopy(cmdarray, 1, args, 0, length);

        process = new UnixProcess(executable, args);
    }

    @VisibleForTesting
    Map<String, String> getEnvironment() {
        return process.getEnvironment();
    }

    /**
     * Adds the specified environment variables.
     *
     * @param environment the variables to add
     * @throws IllegalArgumentException if any value given is null (unsupported)
     */
    public void setEnvironmentVariables(Map<String, String> environment) {
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            setEnvironmentVariable(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds the specified environment variable.
     *
     * @param name  the name of the environment variable
     * @param value the value of the environment variable
     * @throws IllegalArgumentException if the value given is null (unsupported)
     */
    public void setEnvironmentVariable(String name, String value) {
        process.setEnvironmentVariable(name, value);
    }

    public void setDynamicLibraryPath(String newLibraryPath) {
        // because on Windows, it is null according to SingleBrowserLocator.computeLibraryPath()
        if (newLibraryPath != null) {
            setEnvironmentVariable(getLibraryPathPropertyName(), newLibraryPath);
        }
    }

    /**
     * @return The platform specific env property name which contains the library path.
     */
    public static String getLibraryPathPropertyName() {
        Platform current = Platform.getCurrent();

        if (current.is(WINDOWS)) {
            return "PATH";

        } else if (current.is(MAC)) {
            return "DYLD_LIBRARY_PATH";

        } else {
            return "LD_LIBRARY_PATH";
        }
    }

    /**
     * @param executable executable name to be found
     * @return string of the path of the executable
     * @deprecated Use the commandline itself to execute your command.
     */
    @Deprecated
    public static String find(String executable) {
        return new ExecutableFinder().find(executable);
    }

    public void executeAsync() {
        process.executeAsync();
    }

    public void execute() {
        executeAsync();
        waitFor();
    }

    public void waitFor() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new WebDriverException(e);
        }
    }

    public void waitFor(long timeout) {
        try {
            process.waitFor(timeout);
        } catch (InterruptedException e) {
            throw new WebDriverException(e);
        }
    }

    public boolean isSuccessful() {
        return 0 == getExitCode();
    }

    public int getExitCode() {
        return process.getExitCode();
    }

    public String getStdOut() {
        return process.getStdOut();
    }

    /**
     * Destroy the current command.
     *
     * @return The exit code of the command.
     */
    public int destroy() {
        return process.destroy();
    }

    /**
     * Check whether the current command is still executing.
     *
     * @return true if the current command is still executing, false otherwise
     */
    public boolean isRunning() {
        return process.isRunning();
    }

    public void setInput(String allInput) {
        process.setInput(allInput);
    }

    public void setWorkingDirectory(String workingDirectory) {
        process.setWorkingDirectory(new File(workingDirectory));
    }

    @Override
    public String toString() {
        return process.toString();
    }

    public void copyOutputTo(OutputStream out) {
        process.copyOutputTo(out);
    }

    public void checkForError() {
        process.checkForError();
    }
}