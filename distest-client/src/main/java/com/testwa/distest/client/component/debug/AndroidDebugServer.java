package com.testwa.distest.client.component.debug;

import com.testwa.distest.client.command.CommonProcessListener;
import com.testwa.distest.client.component.port.SocatPortProvider;
import com.testwa.distest.client.component.port.TcpIpPortProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stop.DestroyProcessStopper;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;


/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class AndroidDebugServer{
    private static final String SOCAT_CMD = "socat";
    private String deviceId;
    private Future<ProcessResult> future;
    private CommonProcessListener processListener;

    private int tcpipPort;
    private int remotePort;

    public AndroidDebugServer(String deviceId, int tcpipPort, int remotePort){
        this.deviceId = deviceId;
        this.tcpipPort = tcpipPort;
        this.remotePort = remotePort;
        this.processListener = new CommonProcessListener(this.getClass().getName());
    }

    public void start() {
        List<String> commandLine = getSocatCommandLine(this.tcpipPort, this.remotePort);
        try {
            future = new ProcessExecutor()
                    .command(commandLine)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            log.debug(line);
                        }
                    }).listener(processListener)
                    .start().getFuture();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        TcpIpPortProvider.pushPort(this.tcpipPort);
        SocatPortProvider.pushPort(this.remotePort);
        if(future != null) {
            if(processListener.getProcess() != null) {
                DestroyProcessStopper.INSTANCE.stop(processListener.getProcess());
            }
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
        if(future != null) {
            if(future.isCancelled()){
                return false;
            }
            if(future.isDone()){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
}
