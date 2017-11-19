package com.testwa.core.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.testwa.core.os.ExecutableFinder;
import io.appium.java_client.service.local.InvalidServerInstanceException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wen on 16/8/28.
 */
public final class PythonServiceBuilder extends CommonRunnerDriverService.Builder<PythonScriptDriverService, PythonServiceBuilder> {
    public static final String PYTHON_PATH = "PYTHON_HOME";
    private static final String BASH = "bash";
    private static final String CMD_EXE = "cmd.exe";
    private static final String PYTHON = "python";
    private File pyScript;
    private String getPyExecutable;
    private long startupTimeout = 120L;
    private TimeUnit timeUnit;

    public PythonServiceBuilder() {
        this.timeUnit = TimeUnit.SECONDS;
    }

    @Override
    protected String findDefaultExecutable() {
        return checkNotNull(new ExecutableFinder().find(PYTHON),
                        "Unable to find executable for: %s", "python");
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
            throw new InvalidServerInstanceException("The invalid appium node " + absoluteNodePath + " has been defined", new IOException("The node " + absoluteNodePath + "doesn\'t exist"));
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
    protected PythonScriptDriverService createRunnerDriverService(String executable, ImmutableList<String> arguments, ImmutableMap<String, String> env) {
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
