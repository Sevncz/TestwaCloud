package com.testwa.distest.client.component.debug;

import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.port.SocatPortProvider;
import com.testwa.distest.client.component.port.TcpIpPortProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;


/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class AndroidDebugServer{
    private static final String SOCAT_CMD = "socat";
    /** 是否运行 */
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private String deviceId;
    private UTF8CommonExecs execs;
    private int tcpipPort;
    private int remotePort;

    public AndroidDebugServer(String deviceId, int tcpipPort, int remotePort){
        this.deviceId = deviceId;
        this.tcpipPort = tcpipPort;
        this.remotePort = remotePort;
    }

    public void start() {
        if (this.isRunning.get()) {
            throw new IllegalStateException("debug 服务已运行");
        } else {
            this.isRunning.set(true);
        }
        try {
            CommandLine commandLine = getSocatCommandLine(this.tcpipPort, this.remotePort);
            log.info("拉起 socat 服务 shellCommand: {}", commandLine.toString().replace(",", ""));
            execs = new UTF8CommonExecs(commandLine);
            execs.setTimeout(INFINITE_TIMEOUT);
            execs.asyncexec();
        } catch (Exception e) {
            String out = execs.getOutput();
            String error = execs.getError();
            log.warn("socat服务运行异常, {} {} {}", this.deviceId, out, error);
        }
    }

    public void stop() {
        this.isRunning.set(false);
        TcpIpPortProvider.pushPort(this.tcpipPort);
        SocatPortProvider.pushPort(this.remotePort);
        if(execs != null) {
            execs.destroy();
        }
    }


    /**
     * @Description: socat tcp4-listen:15555,fork,reuseaddr tcp-connect:127.0.0.1:5555
     * @Param: [tcpipPort, socatListenPort]
     * @Return: org.apache.commons.exec.CommandLine
     * @Author wen
     * @Date 2019/5/14 22:46
     */
    private CommandLine getSocatCommandLine(int tcpipPort, int socatListenPort) {
        CommandLine commandLine = new CommandLine(SOCAT_CMD);
        commandLine.addArgument(String.format("tcp4-listen:%d,fork,reuseaddr",socatListenPort));
        commandLine.addArgument(String.format("tcp-connect:127.0.0.1:%d",tcpipPort));
        return commandLine;
    }

    public boolean isRunning() {
        return this.isRunning.get();
    }
}
