package com.testwa.distest.client.component.minicap;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.port.MinicapPortProvider;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.Closeable;import java.nio.file.Path;import java.nio.file.Paths;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;/** * @Program: distest * @Description: ios minicap * @Author: wen * @Create: 2018-06-20 10:40 **/@Slf4jpublic class MinicapIOSServer extends Thread implements Closeable {    private String resolution = "400x600";    private Integer port;    private String udid;    private String resourcePath;    private Path ios_minicap_file;    private UTF8CommonExecs execs;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    /** 是否重启 */    private AtomicBoolean restart = new AtomicBoolean(false);    public MinicapIOSServer(String udid) {        super("ios-minicap-server");        this.udid = udid;        this.resourcePath = Config.getString("distest.agent.resources");        this.ios_minicap_file = Paths.get(this.resourcePath,"ios-minicap", "ios_minicap");    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    @Override    public void close() {        execs.destroy();        this.isRunning.set(false);        this.restart.set(true);    }    /**     * 设置分辨率     * @param resolution     */    public void setResolution(String resolution) {        this.resolution = resolution;    }    /**     * 重启     */    public void restart() {        execs.destroy();        this.restart.set(true);    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("iOS Minicap服务已运行");        } else {            this.isRunning.set(true);        }        this.port = MinicapPortProvider.pullPort();        super.start();    }    @Override    public void run() {        while (this.isRunning.get()) {            try {                CommandLine commandLine = getMinicapCommand(this.port);                log.info("拉起 ios Minicap 服务 command: {}", commandLine.toString());                execs = new UTF8CommonExecs(commandLine);                execs.setTimeout(INFINITE_TIMEOUT);                execs.asyncexec();            } catch (Exception e) {                String out = execs.getOutput();                String error = execs.getError();                log.warn("iOS {} Minicap服务运行异常, {} {}", udid, out, error);            }            int tryTime = 20;            while(this.isRunning.get()) {                String out = execs.getOutput();                if(StringUtils.contains(out, "== Banner ==")) {                    try {                        TimeUnit.SECONDS.sleep(2);                    } catch (InterruptedException e) {                    }                    continue;                }else{                    try {                        TimeUnit.SECONDS.sleep(1);                    } catch (InterruptedException e) {                    }                    tryTime--;                }                if(tryTime <= 0) {                    execs.destroy();                    restart.set(true);                    break;                }            }        }        if(this.port != null) {            MinicapPortProvider.pushPort(this.port);        }        this.isRunning.set(false);        log.info("Minicap服务已关闭");    }    private CommandLine getMinicapCommand(int port) {        CommandLine commandLine = new CommandLine(this.ios_minicap_file.toString());        commandLine.addArgument("--udid");        commandLine.addArgument(this.udid);        commandLine.addArgument("--port");        commandLine.addArgument(String.valueOf(port));        commandLine.addArgument("--resolution");        commandLine.addArgument(this.resolution);        return commandLine;    }    public int getPort() {        return this.port;    }}