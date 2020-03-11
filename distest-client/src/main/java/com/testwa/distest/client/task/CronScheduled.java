package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sun.jna.Platform;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptActionEnum;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.util.VoUtil;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
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
import com.testwa.distest.client.ios.IOSDeviceUtil;
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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private ScriptCode scriptCodePython;
    @Autowired
    private CustomAppiumManagerPool customAppiumManagerPool;

    /**
     *@Description: android设备在线情况的补充检查
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/8
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void androidInit() {
        Config.setEnv(env);
        List<JadbDevice> devices = JadbDeviceManager.getJadbDeviceList();
        List<Future<DeviceManager>> resultList = new ArrayList<>();
        devices.forEach(d -> {
            try {
                if(!deviceManagerPool.hasExist(d.getSerial())) {
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
                            String appPath = msg.getAppUrl();

                            CustomAppiumManager manager = customAppiumManagerPool.getManager();
                            String port = manager.getPort() + "";
//                                String appLocalPath = Constant.localAppPath + File.separator + appInfo.getFileAliasName();
//                                Downloader downloader = new Downloader();
//                                downloader.start(appPath, );
                            String scriptContent = scriptGenerator.toAndroidPyScript(templateFunctions, deviceId, platformVersion, appPath, port);
                            runPyScript(msg, scriptContent);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //遍历任务的结果
        for (Future<DeviceManager> fs : resultList){
            while(!fs.isDone()){
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

        if(Platform.isMac()) {
            List<String> udids = IOSDeviceUtil.getUDID();
            log.debug("udids {}", udids.toString());
            List<Future<DeviceManager>> resultList = new ArrayList<>();
            udids.forEach(udid -> {
                try {
                    boolean success = IOSDeviceUtil.addOnline(udid);
                    if(success) {
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
            for (Future<DeviceManager> fs : resultList){
                while(!fs.isDone()){
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
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
                if(manager != null) {
                    if(manager.deviceIsRealOffline()) {
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

    private List<Function> generatorFunctions(TaskVO msg) {
        ScriptCaseVO scriptCaseVO = msg.getScriptCase();
        List<ScriptFunctionVO> functionList = scriptCaseVO.getFunctions();
        List<Function> templateFunctions = new ArrayList<>();
        for (ScriptFunctionVO scriptFunctionVO : functionList) {
            List<ScriptActionVO> actionVOS = scriptFunctionVO.getActions();
            Function function = VoUtil.buildVO(scriptFunctionVO, Function.class);
            function.setActions(null);
            for (ScriptActionVO scriptActionVO : actionVOS) {
                String code = "";
                String action = scriptActionVO.getAction();
                JSONArray jsonArray = JSON.parseArray(scriptActionVO.getParameter());
                if (ScriptActionEnum.findAndAssign.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_findAndAssign(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2), jsonArray.getBoolean(3), msg.getMetadata());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.click.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_click(jsonArray.getString(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.tap.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_tap(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.sendKeys.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_sendKeys(jsonArray.getString(0), jsonArray.getString(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                function.addCode(code);
            }
            templateFunctions.add(function);
        }
        return templateFunctions;
    }

    private void runPyScript(TaskVO msg, String scriptContent) {
        String pyPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + ".py";
        String resultPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + "result";
        String reportPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + "report";
        log.info("python 脚本路径 {} ", pyPath);
        try {
            if (!Files.exists(Paths.get(pyPath))) {
                Files.createFile(Paths.get(pyPath));
            }
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
            try {
                pyexecs.exec();
                // 生成报告 allure generate ./result -o ./report/ --clean
                CommandLine commandLine2 = new CommandLine("allure");
                commandLine2.addArgument("generate");
                commandLine2.addArgument(resultPath);
                commandLine2.addArgument("-o");
                commandLine2.addArgument(reportPath);
                commandLine2.addArgument("--clean");
                pyexecs = new UTF8CommonExecs(commandLine2);
                pyexecs.setTimeout(60 * 1000L);
                pyexecs.exec();
                // 上传报告

            } catch (IOException e) {
                String output = pyexecs.getOutput();
                log.error(output);
                log.error("py 执行失败", e);
            }
        } catch (IOException e) {
            log.error("py 写入失败", e);
        }
    }

}
