package com.testwa.distest.client.component.debug;

import com.testwa.distest.client.util.CommonProcessListener;
import com.testwa.distest.client.component.port.SocatPortProvider;
import com.testwa.distest.client.component.port.TcpIpPortProvider;
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
public class AndroidDebugServer{
    private static final String SOCAT_CMD = "/usr/local/bin/socat";
    private StartedProcess mainProcess;
    private CommonProcessListener processListener;

    private int tcpipPort;
    private int remotePort;

    public AndroidDebugServer(int tcpipPort, int remotePort){
        this.tcpipPort = tcpipPort;
        this.remotePort = remotePort;
        this.processListener = new CommonProcessListener(this.getClass().getName());
    }

    public void start() {
        List<String> commandLine = getSocatCommandLine(this.tcpipPort, this.remotePort);
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
        TcpIpPortProvider.pushPort(this.tcpipPort);
        SocatPortProvider.pushPort(this.remotePort);
        if(mainProcess != null) {
            CommandLineExecutor.processQuit(mainProcess);
        }
    }


    /**
     * @Description: socat tcp4-listen:15555,fork,reuseaddr tcp-connect:127.0.0.1:5555
     * @Param: [tcpipPort, socatListenPort]
     * @Return: org.apache.commons.exec.CommandLine
     * @Author wen
     * @Date 2019/5/14 22:46
     */
    private List<String> getSocatCommandLine(int tcpipPort, int socatListenPort) {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(SOCAT_CMD);
        commandLine.add(String.format("tcp4-listen:%d,fork,reuseaddr",socatListenPort));
        commandLine.add(String.format("tcp-connect:127.0.0.1:%d",tcpipPort));
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
