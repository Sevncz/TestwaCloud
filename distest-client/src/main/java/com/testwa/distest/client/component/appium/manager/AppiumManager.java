package com.testwa.distest.client.component.appium.manager;import com.testwa.core.utils.Identities;import com.testwa.distest.client.component.port.AppiumPortProvider;import com.testwa.distest.client.component.Constant;import io.appium.java_client.service.local.AppiumDriverLocalService;import io.appium.java_client.service.local.AppiumServiceBuilder;import io.appium.java_client.service.local.flags.AndroidServerFlag;import io.appium.java_client.service.local.flags.GeneralServerFlag;import lombok.Data;import lombok.extern.slf4j.Slf4j;import java.io.File;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;@Slf4j@Datapublic class AppiumManager {    private final String id = Identities.uuid();    private AppiumDriverLocalService appiumService;    private String nodePath;    private String appiumPath;    private String appiumlogPath;    private String clientWebUrl;    private int port;    private int bootstrapPort;    public AppiumManager(String nodePath, String appiumPath, String clientWebUrl) throws IOException {        this.nodePath = nodePath;        this.appiumPath = appiumPath;        this.clientWebUrl = clientWebUrl;        this.port = AppiumPortProvider.pullPort();        this.bootstrapPort = AppiumPortProvider.pullPort();        init();        appiumService.start();    }    private void init() throws IOException {        this.appiumlogPath = getAppiumlogPath(port + "");        AppiumServiceBuilder builder =                new AppiumServiceBuilder()                        .usingDriverExecutable(new File(nodePath))                        .withAppiumJS(new File(appiumPath))                        .withArgument(GeneralServerFlag.LOG_LEVEL, "info").withLogFile(new File(appiumlogPath))                        .withArgument(AndroidServerFlag.SUPPRESS_ADB_KILL_SERVER)//                        .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER, Integer.toString(bootstrapPort))//                        .withArgument(CustomServerFlag.SCREEN_PATH, Constant.localScreenshotPath)//                        .withArgument(CustomServerFlag.FOR_PORTAL, clientWebUrl)                        .withArgument(CustomServerFlag.NO_RESET)                        .withArgument(CustomServerFlag.UNLOCK_TYPE)                        .withArgument(CustomServerFlag.PORT, port+"")                        .usingPort(port);        appiumService = builder.build();    }    private String getAppiumlogPath(String port) throws IOException {        Path appiumlogDir = Paths.get(Constant.localAppiumLogPath, port);        if(!Files.exists(appiumlogDir)){            Files.createDirectories(appiumlogDir);        }        String logpath = Paths.get(appiumlogDir.toString(), String.format("appium_%s.log", Identities.randomLong())).toString();        log.info("Appium log path: {}", logpath);        return logpath;    }    public Boolean appiumIsRunning(){        return this.appiumService.isRunning();    }    public void appiumStart() {        if(!this.appiumService.isRunning()){            this.appiumService.start();        }    }    public void destory(){        this.appiumService.stop();    }    public void reset() throws IOException {        if(this.appiumService != null){            this.appiumService.stop();        }else{            this.init();        }        this.appiumService.start();    }    @Override    public boolean equals(Object o) {        if (this == o)            return true;        if (!(o instanceof AppiumManager))            return false;        AppiumManager that = (AppiumManager) o;        if (id != null ? !id.equals(that.id) : that.id != null)            return false;        return true;    }    @Override    public int hashCode() {        return id != null ? id.hashCode() : 0;    }}