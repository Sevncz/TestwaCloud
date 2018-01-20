package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by wen on 16/8/28.
 */
public final class PythonServiceBuilder extends CommonRunnerDriverService.Builder<PythonScriptDriverService, PythonServiceBuilder> {
    public static final String PYTHON_HOME = "PYTHON_HOME";
    private static final String PYTHON = "python";
    private File pyScript;
    private long startupTimeout = 120L;
    private TimeUnit timeUnit;

    public PythonServiceBuilder() {
        this.timeUnit = TimeUnit.SECONDS;
    }

    @Override
    protected File findDefaultExecutable() {
        String pythonHome = System.getProperty(PYTHON_HOME);
        if (StringUtils.isBlank(pythonHome)) {
            pythonHome = System.getenv(PYTHON_HOME);
        }
        if (!StringUtils.isBlank(pythonHome)) {
            File result = new File(pythonHome);
            if (result.exists()) {
                return result;
            }
        }
        CommandLine commandLine;
        try {
            if (Platform.getCurrent().is(Platform.WINDOWS)) {
                commandLine = new CommandLine(PYTHON, "-c", "import sys; print sys.executable");
            } else {
                commandLine = new CommandLine(PYTHON, "-c", "import sys; print sys.executable");
            }
            commandLine.execute();
        } catch (Throwable t) {
            throw new InvalidPythonInstance("Python is not installed!", t);
        }


        String filePath = (commandLine.getStdOut()).trim();

        try {
            if (StringUtils.isBlank(filePath) || !new File(filePath).exists()) {
                String errorOutput = commandLine.getStdOut();
                String errorMessage = "Can't get a path to the default Python instance";
                throw new InvalidPythonInstance(errorMessage, new IOException(errorOutput));
            }
            return new File(filePath);
        } finally {
            commandLine.destroy();
        }

    }

    void checkPyScript() {
        if(this.pyScript != null) {
            validatePyStructure(this.pyScript);
        }else{
            throw new InvalidServerInstanceException("The invalid python", new IOException("The script doesn\'t exist"));
        }
    }

    private static void validatePyStructure(File node) {
        String absoluteNodePath = node.getAbsolutePath();
        if(!node.exists()) {
            throw new InvalidServerInstanceException("The invalid python exe " + absoluteNodePath + " has been defined", new IOException("The node " + absoluteNodePath + "doesn\'t exist"));
        }
    }

    @Override
    protected ImmutableList<String> createArgs() {
        List<String> argList = new ArrayList<>();
        checkPyScript();
        argList.add(this.pyScript.getAbsolutePath());
        return (new ImmutableList.Builder()).addAll(argList).build();
    }

    @Override
    protected PythonScriptDriverService createDriverService(File executable, int i, ImmutableList<String> arguments, ImmutableMap<String, String> env) {
        try {
            return new PythonScriptDriverService(executable, arguments, env, this.startupTimeout, this.timeUnit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PythonServiceBuilder withPyScript(File pyScript) {
        this.pyScript = pyScript;
        return this;
    }

}
