package com.testwa.distest.client.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * @author wen
 * @create 2019-05-20 16:05
 */
@Slf4j
@Data
public class CommonProcessListener extends ProcessListener {
    private Process process;
    private String command;

    public CommonProcessListener(String command) {
        this.command = command;
    }

    @Override
    public void beforeStart(ProcessExecutor executor) {
        super.beforeStart(executor);
        log.info("[ProcessExecutor ready start] {}", command);
    }

    @Override
    public void afterStart(Process process, ProcessExecutor executor) {
        log.info("[ProcessExecutor started] {}", command);
        this.process = process;
    }

    @Override
    public void afterFinish(Process process, ProcessResult result) {
        log.info("[ProcessExecutor finished] {} exitCode: {} result: {}", command, result.getExitValue(), String.join("\n", result.getOutput().getLinesAsUTF8()));
    }

    @Override
    public void afterStop(Process process) {
        log.info("[ProcessExecutor stoped] {}", command);
    }
}
