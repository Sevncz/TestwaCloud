package com.testwa.distest.client.command;

import com.testwa.distest.client.component.wda.remote.WebDriverAgentRunner;
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
public class WdaProcessListener extends ProcessListener {
    private Process process;
    private String className;
    private WebDriverAgentRunner runner;

    public WdaProcessListener(WebDriverAgentRunner wdaServer) {
        this.className = wdaServer.getClass().getName();
        this.runner = wdaServer;
    }

    @Override
    public void afterStart(Process process, ProcessExecutor executor) {
        log.info("[{}] ProcessExecutor start ... ... ", className);
        this.process = process;
    }

    @Override
    public void afterFinish(Process process, ProcessResult result) {
        log.info("[{}] ProcessExecutor finish {}... ... ", className, result.getOutput().getLinesAsUTF8());
    }

    @Override
    public void afterStop(Process process) {
        if(runner.isStop()) {
            log.info("[{}] ProcessExecutor stop ... ... ", className);
        }else{
            runner.start();
        }
    }
}
