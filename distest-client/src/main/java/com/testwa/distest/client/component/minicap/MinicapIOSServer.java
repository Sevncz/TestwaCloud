package com.testwa.distest.client.component.minicap;import com.testwa.distest.client.util.CommonProcessListener;import com.testwa.distest.client.util.CommandLineExecutor;import com.testwa.distest.client.ios.IOSDeviceUtil;import com.testwa.distest.client.ios.IOSPhysicalSize;import com.testwa.distest.client.util.PortUtil;import lombok.extern.slf4j.Slf4j;import org.zeroturnaround.exec.ProcessExecutor;import org.zeroturnaround.exec.StartedProcess;import org.zeroturnaround.exec.stream.LogOutputStream;import java.io.IOException;import java.nio.file.Path;import java.nio.file.Paths;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: ios minicap * @Author: wen * @Create: 2018-06-20 10:40 **/@Slf4jpublic class MinicapIOSServer{    private String resolution;    private Integer port;    private String udid;    private String resourcePath;    private Path iosMinicapFile;    private StartedProcess process;    public MinicapIOSServer(String udid, String resourcePath) {        this.udid = udid;        IOSPhysicalSize size = IOSDeviceUtil.getSize(udid);        if(size != null) {            this.resolution = String.format("%dx%d", size.getPhsicalWidth(), size.getPhsicalHeight());        }else{            this.resolution =  "750x1334";        }        this.resourcePath = resourcePath;        this.iosMinicapFile = Paths.get(this.resourcePath,"ios-minicap", "ios_minicap");    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        if(process != null) {            return process.getProcess().isAlive();        }else{            return false;        }    }    public void close() {        if(process != null) {            CommandLineExecutor.processQuit(process);        }    }    public void restart() {        close();        start();    }    public synchronized void start() {        try {            this.port = PortUtil.getAvailablePort();            List<String> commandLine = getMinicapCommandList(this.port);            log.info("拉起 ios Minicap 服务 shellCommand: {}", commandLine.toString().replace(",", ""));            CommonProcessListener processListener = new CommonProcessListener(String.join(" ", commandLine));            process = new ProcessExecutor()                    .command(commandLine)                    .redirectOutput(new LogOutputStream() {                        @Override                        protected void processLine(String line) {                            log.info("iOS Minicap {}", line);                        }                    }).listener(processListener)                    .start();        } catch (IOException e) {            throw new IllegalStateException("iOS Minicap 服务启动失败");        }    }    private List<String> getMinicapCommandList(int port) {        List<String> command = new ArrayList<>();        command.add(this.iosMinicapFile.toString());        command.add("--udid");        command.add(this.udid);        command.add("--port");        command.add(String.valueOf(port));        command.add("--resolution");        command.add(this.resolution);        return command;    }    public int getPort() {        return this.port;    }}