package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.sun.jna.Platform;
import com.testwa.core.script.EnvGenerator;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.util.FileUtil;
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
import com.testwa.distest.client.device.manager.DeviceManager;
import com.testwa.distest.client.device.pool.DeviceManagerPool;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.DeviceGvice;
import com.testwa.distest.client.util.PortUtil;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import io.rpc.testwa.device.DeviceStatusChangeRequest;
import io.rpc.testwa.device.DeviceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    private EnvGenerator envGenerator;
    @Autowired
    private ScriptCode scriptCodePython;
    @Autowired
    private CustomAppiumManagerPool customAppiumManagerPool;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${distest.api.web}")
    private String apiUrl;
    @Value("${download.url}")
    private String downloadUrl;
    @Value("${application.version}")
    private String applicationVersion;

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
                        @SneakyThrows
                        @Override
                        public void onMessage(CharSequence channel, TaskVO msg) {
                            log.info("监听到消息: {}", JSON.toJSONString(msg));
                            // 生成脚本
                            List<Function> templateFunctions = generatorFunctions(msg);

                            String deviceId = msg.getDeviceId();
                            String platformVersion = ADBCommandUtils.getPlatformVersion(deviceId);
                            String appLocalPath = downloadApp(msg.getAppUrl());

                            CustomAppiumManager manager = customAppiumManagerPool.getManager();
                            String port = manager.getPort() + "";
                            String scriptContent = scriptGenerator.toAndroidPyScript(templateFunctions, deviceId, platformVersion, appLocalPath, port);
                            runPyScript(msg, scriptContent);
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
                                List<Function> templateFunctions = generatorFunctions(msg);

                                String udid = msg.getDeviceId();
                                String platformVersion = "13.3";
                                String xcodeOrgId = "UNW569G4GD";
                                String appPath = msg.getAppUrl();

                                CustomAppiumManager manager = customAppiumManagerPool.getManager();
                                String port = manager.getPort() + "";
                                String wdaLocalPort = PortUtil.getAvailablePort() + "";
                                String mjpegServerPort = PortUtil.getAvailablePort() + "";
//                                String appLocalPath = Constant.localAppPath + File.separator + appInfo.getFileAliasName();
//                                Downloader downloader = new Downloader();
//                                downloader.start(appPath, );
                                String scriptContent = scriptGenerator.toIosPyScript(templateFunctions, udid, xcodeOrgId, platformVersion, appPath, port, wdaLocalPort, mjpegServerPort);
                                runPyScript(msg, scriptContent);
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

    private List<Function> generatorFunctions(TaskVO msg) throws Exception {
        String url = "http://" + apiUrl + "/v2/script/" + msg.getScriptCase().getScriptCaseId() + "/pyActionCode";
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("X-TOKEN", UserInfo.token);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> request = new HttpEntity<>(requestHeaders);
        ResponseEntity<FunctionCodeEntity> responseEntity = this.restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                FunctionCodeEntity.class
        );
        if (responseEntity.getStatusCode().value() == 200 && responseEntity.getBody().getCode() == 0) {
            return responseEntity.getBody().getData();
        }
        throw new Exception("代码生成异常");
    }

    private void runPyScript(TaskVO msg, String scriptContent) {
        String pyPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + ".py";
        String resultPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + "result";
        log.info("python 脚本路径 {} ", pyPath);
        try {
            if (!Files.exists(Paths.get(pyPath))) {
                Files.createFile(Paths.get(pyPath));
            }
            FileUtil.ensureExistEmptyDir(resultPath);
            Files.write(Paths.get(pyPath), scriptContent.getBytes());
            CommandLine commandLine = new CommandLine("pytest");
            commandLine.addArgument(pyPath);
            commandLine.addArgument("--alluredir");
            commandLine.addArgument(resultPath);
//                                    commandLine.addArgument("-reruns");
//                                    commandLine.addArgument("3");
            UTF8CommonExecs pyexecs = new UTF8CommonExecs(commandLine);
            // 设置最大执行时间，30分钟
            pyexecs.setTimeout(30 * 60 * 1000L);
            int success = 0;
            try {
                pyexecs.exec();
            } catch (IOException e) {
                success = success + 1;
                String output = pyexecs.getOutput();
                log.error(output);
                log.error("py 执行失败", e);
            } finally {
                // 上传result json
                uploadResult(msg, resultPath, success);
            }
        } catch (IOException e) {
            log.error("py 写入失败", e);
        }
    }

    private void uploadResult(TaskVO msg, String resultPath, int success) throws IOException {

        if (Files.list(Paths.get(resultPath)).count() > 0) {
            Files.list(Paths.get(resultPath)).forEach(f -> {
                String url = "http://" + apiUrl + "/v1/fileSupport/single";
                HttpHeaders requestHeadersFile = new HttpHeaders();
                requestHeadersFile.set("X-TOKEN", UserInfo.token);
                FileSystemResource resource = new FileSystemResource(f.toFile());
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                param.add("file", resource);
                HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, requestHeadersFile);
                ResponseEntity<FileUploadEntity> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, FileUploadEntity.class);
                log.info("上传返回：{}", responseEntity.getBody().getData());
                if (responseEntity.getStatusCode().value() == 200 && responseEntity.getBody().getCode() == 0) {
                    // 生成TaskResult对象
                    TaskResultVO resultVO = new TaskResultVO();
                    resultVO.setResult(f.getFileName().toString().replace(resultPath, ""));
                    resultVO.setTaskCode(msg.getTaskCode());
                    resultVO.setUrl(responseEntity.getBody().getData());
                    resultVO.setDeviceId(msg.getDeviceId());
                    String url2 = "http://" + apiUrl + "/v2/task/result";
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.set("X-TOKEN", UserInfo.token);
                    requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                    HttpEntity<String> formEntity = new HttpEntity<>(JSON.toJSONString(resultVO), requestHeaders);
                    ResponseEntity<String> responseEntity1 = this.restTemplate.postForEntity(url2, formEntity, String.class);
                    log.info("保存TaskResult返回：{}", responseEntity1.getBody());
                }
            });
        }
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
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, formEntity, String.class);
        log.info("生成报告返回：{}", responseEntity.getBody());
    }

    private String downloadApp(String appPath) {
        String appUrl = String.format("http://%s/app/%s", downloadUrl, appPath);
        String appLocalPath = Constant.localAppPath + File.separator + appPath;

        // 检查是否有和该app md5一致的
        try {
            log.info("应用路径：{}", appLocalPath);
            if (Files.notExists(Paths.get(appLocalPath))) {
                Downloader d = new Downloader();
                d.start(appUrl, appLocalPath);
            }
        } catch (DownloadFailException | IOException e) {
            e.printStackTrace();
        }
        return appLocalPath;
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
