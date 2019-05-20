package com.testwa.distest.client.command;

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
    private String className;

    public CommonProcessListener(String className) {
        this.className = className;
    }

    @Override
    public void afterStart(Process process, ProcessExecutor executor) {
        log.info("[{}] ProcessExecutor start ... ... ", className);
        this.process = process;
    }

    @Override
    public void afterFinish(Process process, ProcessResult result) {
        log.info("[{}] ProcessExecutor finish {} {}... ... ", className, result.getExitValue(), result.getOutput());
    }

    @Override
    public void afterStop(Process process) {
        log.info("[{}] ProcessExecutor stop ... ... ", className);
    }
}
