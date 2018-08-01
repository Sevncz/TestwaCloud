package com.testwa.distest.client.component.executor.worker;import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.serializer.SerializerFeature;import com.fasterxml.jackson.databind.JsonNode;import com.fasterxml.jackson.databind.ObjectMapper;import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;import com.testerhome.appcrawler.AppCrawler;import com.testerhome.appcrawler.Crawler;import com.testerhome.appcrawler.CrawlerConf;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.appcrawler.JCrawlerConfig;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.executor.ExecutorLog;import com.testwa.distest.client.exception.InstallAppException;import com.testwa.distest.client.exception.LaunchAppException;import io.appium.java_client.remote.AndroidMobileCapabilityType;import io.rpc.testwa.task.ExecutorAction;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.IOException;import java.nio.file.Path;import java.nio.file.Paths;/** * @Program: distest * @Description: 遍历测试执行器 * @Author: wen * @Create: 2018-07-19 18:35 **/@Slf4jpublic class CrawlerExecutor extends CrawlerAbstractExecutor{    private String appiumUrl;    private Long taskCode;    private Crawler crawler;    @Override    public void init(String appiumUrl, RemoteRunCommand cmd) {        this.appiumUrl = appiumUrl;        this.taskCode = cmd.getTaskCode();        this.crawler = new Crawler();        super.init(cmd);    }    @ExecutorLog(action = ExecutorAction.downloadApp)    public void downloadApp() {        super.downloadApp();    }    @Override    public void start() {        try {            loggerStart();            installApp();            launch(); // 这里启动是为了点掉启动之后的权限弹框            run();            complete();        }catch (InstallAppException e){            // 安装失败            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "没有错误信息";            }            log.error("【遍历测试】设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(taskCode, deviceId, error);        } catch (Exception e) {            // 未知错误            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "未知错误信息";            }            log.error("【遍历测试】设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(taskCode, deviceId, error);        } finally {            loggerStop();            ui2ServerStop();        }    }    @ExecutorLog(action = ExecutorAction.installApp)    public void installApp() throws InstallAppException {        super.installApp();    }    @ExecutorLog(action = ExecutorAction.launch)    public void launch() throws LaunchAppException {        super.launch();    }    @ExecutorLog(action = ExecutorAction.run)    @Override    public void run() {        // 为了避免干扰测试流程，比如 appium 会使用到 uiautomator，关闭我们自己的 uiserver        ui2ServerStop();        Path resultDir = Paths.get(Constant.localCrawlerOutPath);        JCrawlerConfig jCrawlerConfig = new JCrawlerConfig();        jCrawlerConfig.setReportTitle(appInfo.getDisplayName());        jCrawlerConfig.setResultDir(resultDir.toString());//        jCrawlerConfig.getPluginList().add("com.testerhome.appcrawler.plugin.ReportPlugin");        jCrawlerConfig.getCapability().put("app", this.appLocalPath);        jCrawlerConfig.getCapability().put("appium", this.appiumUrl);        jCrawlerConfig.getCapability().put("platformName", "Android");        jCrawlerConfig.getCapability().put("automationName", "uiautomator2");        jCrawlerConfig.getCapability().put("deviceName", device.getName());        jCrawlerConfig.getCapability().put("udid", device.getSerialNumber());        jCrawlerConfig.getCapability().put("newCommandTimeout", 20);        jCrawlerConfig.getCapability().put("autoLaunch", true);//        jCrawlerConfig.getAndroidCapability().put(AndroidMobileCapabilityType.APP_PACKAGE, "com.netease.cloudmusic");//        jCrawlerConfig.getAndroidCapability().put(AndroidMobileCapabilityType.APP_ACTIVITY, "com.netease.cloudmusic.activity.LoadingActivity");        jCrawlerConfig.getAndroidCapability().put(AndroidMobileCapabilityType.DONT_STOP_APP_ON_RESET, true);        jCrawlerConfig.getAndroidCapability().put(AndroidMobileCapabilityType.NO_SIGN, false);//        jCrawlerConfig.getBlackList().add("//android.widget.TextView[@resource-id='com.netease.cloudmusic:id/aag']");//        jCrawlerConfig.addTriggerAction("18600753024", "//*[contains(@text,'请输入手机号')]", 1);//        jCrawlerConfig.addTriggerAction("wen19880528", "//*[contains(@text,'请输入密码')]", 1);//        jCrawlerConfig.addTriggerAction("click", "//*[contains(@text,'登录')]", 1);//        jCrawlerConfig.getFirstList().add("//*[@resource-id='com.netease.cloudmusic:id/ki']");//        jCrawlerConfig.getFirstList().add("//*[@resource-id='com.netease.cloudmusic:id/kh']");        String jsonNodeString = JSON.toJSONString(jCrawlerConfig, SerializerFeature.WriteMapNullValue);        try {            JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonNodeString);            String yaml = new YAMLMapper().writeValueAsString(jsonNodeTree);            CrawlerConf crawlerConf = new CrawlerConf();            crawlerConf = crawlerConf.loadYaml(yaml);            crawlerConf.androidInit();            AppCrawler.setGlobalEncoding();            log.info(crawlerConf.toYaml());            crawler.loadConf(crawlerConf);            crawler.start(null);        } catch (IOException e) {            log.error("【遍历测试】配置文件解析失败");        }    }    @Override    public void stop() {        if(crawler != null) {            crawler.stop();        }    }    @ExecutorLog(action = ExecutorAction.complete)    public void complete() {        super.complete();    }}