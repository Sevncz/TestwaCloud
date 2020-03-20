package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Platform;
import com.testwa.core.script.EnvGenerator;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.util.FileUtil;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.TaskEnvVO;
import com.testwa.core.script.vo.TaskResultVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.android.JadbDeviceManager;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.appium.manager.CustomAppiumManager;
import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPool;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.executor.uiautomator2.Ui2Command;
import com.testwa.distest.client.component.executor.uiautomator2.Ui2Server;
import com.testwa.distest.client.device.manager.DeviceManager;
import com.testwa.distest.client.device.pool.DeviceManagerPool;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.DeviceGvice;
import com.testwa.distest.client.support.OkHttpUtil;
import com.testwa.distest.client.util.PortUtil;
import com.testwa.distest.client.web.LoginService;
import com.testwa.distest.client.web.startup.EnvCheck;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import io.rpc.testwa.device.DeviceStatusChangeRequest;
import io.rpc.testwa.device.DeviceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class CronScheduled {
    private ExecutorService deviceExecutor = Executors.newCachedThreadPool();

    @Autowired
    private Environment env;
    @Autowired
    private DeviceManagerPool deviceManagerPool;
    @Autowired
    private DeviceGvice deviceGvice;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ScriptGenerator scriptGenerator;
    @Autowired
    private CustomAppiumManagerPool customAppiumManagerPool;
    @Autowired
    private PyTaskProvider pyTaskProvider;
    @Autowired
    private BaseProvider baseProvider;
    @Value("${application.version}")
    private String applicationVersion;
    @Value("${distest.api.web}")
    private String apiUrl;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoginService loginService;


    /**
     * @Description: android设备在线情况的补充检查
     * @Param: []
     * @Return: void
     * @Author: wen
     * @Date: 2018/5/8
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void androidInit() {
        Config.setEnv(env);
        List<JadbDevice> devices = JadbDeviceManager.getJadbDeviceList();
        List<Future<DeviceManager>> resultList = new ArrayList<>();
        devices.forEach(d -> {
            try {
                if (!deviceManagerPool.hasExist(d.getSerial())) {
                    Future<DeviceManager> future = deviceExecutor.submit(new DeviceManagerTask(d.getSerial(), DeviceType.ANDROID));
                    resultList.add(future);
                    TimeUnit.MILLISECONDS.sleep(100);

                    RTopic rTopic = redissonClient.getTopic(d.getSerial());
                    log.info("[{}]增加任务监听", d.getSerial());
                    rTopic.addListener(TaskVO.class, new MessageListener<TaskVO>() {
                        @Override
                        public void onMessage(CharSequence channel, TaskVO msg) {
                            log.info("监听到消息: {}", JSON.toJSONString(msg));
                            CustomAppiumManager manager = customAppiumManagerPool.getManager();
                            String port = manager.getPort() + "";

                            String deviceId = msg.getDeviceId();
                            String platformVersion = ADBCommandUtils.getPlatformVersion(deviceId);
                            String appLocalPath = pyTaskProvider.downloadApp(msg.getAppUrl());
                            int success = 0;
                            try {
                                List<List<Function>> templateFunctions = new ArrayList<>();
                                String systemPort = String.valueOf(PortUtil.getAvailablePort());
                                // 生成脚本
                                for (ScriptCaseVO scriptCase : msg.getScriptCases()) {
                                    List<Function> functions = scriptGenerator.getFunctions(scriptCase, msg.getMetadata());
                                    templateFunctions.add(functions.stream().peek(f -> f.setScriptCaseName(scriptCase.getScriptCaseName())).collect(Collectors.toList()));
                                }
                                String scriptContent = scriptGenerator.toAndroidPyScript(msg.getScriptCases(), templateFunctions, deviceId, platformVersion, appLocalPath, port, systemPort);
                                pyTaskProvider.runPyScript(msg, scriptContent);
                            }catch (Exception e) {
                                success += 1;
                            }finally {
                                // 上传完成，通知任务已完成
                                TaskEnvVO envVO = new TaskEnvVO();
                                AgentInfo agentInfo = AgentInfo.getAgentInfo();
                                envVO.setAgentVersion(applicationVersion);
                                envVO.setJavaVersion(agentInfo.getJavaVersion());
                                envVO.setOsVersion(agentInfo.getOsVersion());
                                envVO.setNodeVersion("1.13");
                                envVO.setPythonVersion("3.7");
                                envVO.setDeviceId(msg.getDeviceId());

                                String url = "http://" + apiUrl + "/v2/task/" + msg.getTaskCode() + "/finish/" + success;
                                HttpHeaders requestHeaders = new HttpHeaders();
                                requestHeaders.set("X-TOKEN", UserInfo.token);
                                requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                                HttpEntity<String> formEntity = new HttpEntity<>(JSON.toJSONString(envVO), requestHeaders);
                                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, formEntity, String.class);
                                log.info("通知任务结束：{}", responseEntity.getBody());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //遍历任务的结果
        for (Future<DeviceManager> fs : resultList) {
            while (!fs.isDone()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void iOSInit() {
        Config.setEnv(env);

        if (Platform.isMac()) {
            List<String> udids = IOSDeviceUtil.getUDID();
            log.debug("udids {}", udids.toString());
            List<Future<DeviceManager>> resultList = new ArrayList<>();
            udids.forEach(udid -> {
                try {
                    boolean success = IOSDeviceUtil.addOnline(udid);
                    if (success) {
                        RTopic rTopic = redissonClient.getTopic(udid);
                        log.info("[{}]增加任务监听", udid);
                        rTopic.addListener(TaskVO.class, new MessageListener<TaskVO>() {
                            @SneakyThrows
                            @Override
                            public void onMessage(CharSequence channel, TaskVO msg) {
                                log.info("监听到消息: {}", JSON.toJSONString(msg));
                                // 生成脚本
                                List<List<Function>> templateFunctions = new ArrayList<>();
                                String systemPort = String.valueOf(PortUtil.getAvailablePort());
                                // 生成脚本
                                for (ScriptCaseVO scriptCase : msg.getScriptCases()) {
                                    List<Function> functions = scriptGenerator.getFunctions(scriptCase, msg.getMetadata());
                                    templateFunctions.add(functions.stream().peek(f -> f.setScriptCaseName(scriptCase.getScriptCaseName())).collect(Collectors.toList()));
                                }

                                String udid = msg.getDeviceId();
                                String platformVersion = "13.3";
                                String xcodeOrgId = "UNW569G4GD";
                                String appLocalPath = pyTaskProvider.downloadApp(msg.getAppUrl());

                                CustomAppiumManager manager = customAppiumManagerPool.getManager();
                                String port = manager.getPort() + "";
                                String wdaLocalPort = PortUtil.getAvailablePort() + "";
                                String mjpegServerPort = PortUtil.getAvailablePort() + "";
                                String scriptContent = scriptGenerator.toIosPyScript(templateFunctions, udid, xcodeOrgId, platformVersion, appLocalPath, port, wdaLocalPort, mjpegServerPort);
                                pyTaskProvider.runPyScript(msg, scriptContent);
                            }
                        });
                    }
                    Future<DeviceManager> future = deviceExecutor.submit(new DeviceManagerTask(udid, DeviceType.IOS));
                    resultList.add(future);
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //遍历任务的结果
            for (Future<DeviceManager> fs : resultList) {
                while (!fs.isDone()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void iOSClear() {
        for (String udid : IOSDeviceUtil.ONLINE_UDID) {
            if (!IOSDeviceUtil.isOnline(udid)) {
                log.warn("iOS 设备 {} 离线", udid);
                RTopic rTopic = redissonClient.getTopic(udid);
                rTopic.removeAllListeners();
                IOSDeviceUtil.removeOnline(udid);
                deviceManagerPool.release(udid);
                deviceGvice.stateChange(udid, DeviceStatusChangeRequest.LineStatus.DISCONNECTED);
            }
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void androidClear() {
        List<JadbDevice> devices = JadbDeviceManager.getJadbDeviceList();
        for (JadbDevice jadbDevice : devices) {
            try {
                JadbDevice.State state = jadbDevice.getState();
            } catch (IOException | JadbException e) {
                DeviceManager manager = deviceManagerPool.getInitialManager(jadbDevice.getSerial());
                if (manager != null) {
                    if (manager.deviceIsRealOffline()) {
                        deviceManagerPool.release(manager);
                        JadbDeviceManager.jadbDeviceMap.remove(jadbDevice.getSerial());
                        deviceGvice.stateChange(jadbDevice.getSerial(), DeviceStatusChangeRequest.LineStatus.DISCONNECTED);
                        RTopic rTopic = redissonClient.getTopic(jadbDevice.getSerial());
                        rTopic.removeAllListeners();
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void login() {
        loginService.login();
    }

    class DeviceManagerTask implements Callable<DeviceManager> {
        private String deviceId;
        private DeviceType type;

        public DeviceManagerTask(String deviceId, DeviceType type) {
            this.deviceId = deviceId;
            this.type = type;
        }

        @Override
        public DeviceManager call() throws Exception {
            return deviceManagerPool.getManager(deviceId, type);
        }
    }
}
