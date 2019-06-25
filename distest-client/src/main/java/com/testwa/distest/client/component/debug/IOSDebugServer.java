package com.testwa.distest.client.component.debug;

import com.testwa.distest.client.util.CommonProcessListener;
import com.testwa.distest.client.util.CommandLineExecutor;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class IOSDebugServer {
    private static final String SOCAT_CMD = "/usr/local/bin/socat";
    private StartedProcess mainProcess;

    private int remotePort;

    public IOSDebugServer(int remotePort){
        this.remotePort = remotePort;
    }

    public void start() {
        List<String> commandLine = getSocatCommandLine(this.remotePort);
        CommonProcessListener processListener = new CommonProcessListener(String.join(" ", commandLine));
        try {
            mainProcess = new ProcessExecutor()
                    .command(commandLine)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            log.debug(line);
                        }
                    }).listener(processListener)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        if(mainProcess != null) {
            CommandLineExecutor.processQuit(mainProcess);
        }
    }


    /**
     * @Description: socat TCP-LISTEN:8555,reuseaddr,fork UNIX-CONNECT:/var/run/usbmuxd
     * @Param: [tcpipPort, socatListenPort]
     * @Return: org.apache.commons.exec.CommandLine
     * @Author wen
     * @Date 2019/5/14 22:46
     */
    private List<String> getSocatCommandLine(int socatListenPort) {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(SOCAT_CMD);
        commandLine.add(String.format("TCP-LISTEN:%d,fork",socatListenPort));
        commandLine.add("UNIX-CONNECT:/var/run/usbmuxd");
        return commandLine;
    }

    public boolean isRunning() {
        if(mainProcess != null) {
            return mainProcess.getProcess().isAlive();
        }else{
            return false;
        }
    }
}
