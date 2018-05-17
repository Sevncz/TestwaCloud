package com.testwa.distest.client.component.executor;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.component.appium.AppiumManager;import com.testwa.distest.client.component.appium.pool.AppiumManagerPool;import com.testwa.distest.client.component.executor.factory.AndroidExecutorFactory;import com.testwa.distest.client.component.executor.factory.HGAbstractExecutor;import com.testwa.distest.client.exception.DownloadFailException;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;import java.io.IOException;import java.util.ArrayList;import java.util.List;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;@Slf4jpublic class HGTestTask extends AbstractTestTask{    private List<TestTaskListener> listenerList = new ArrayList<>();    private Thread taskInitialThread;    private RemoteRunCommand cmd;    private HGAbstractExecutor executor;    private AppiumManagerPool pool;    private AppiumManager manager;    public HGTestTask(RemoteRunCommand cmd){        this.cmd = cmd;    }    public void start() {        log.info("设备 {} 开始测试任务", cmd.getDeviceId());        pool = (AppiumManagerPool) ApplicationContextUtil.getBean("appiumManagerPool");        GrpcClientService grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        manager = pool.getManager();        String appiumLogPath = manager.getAppiumlogPath();        if(manager.getAppiumService().isRunning()){            try {                String appiumUrl = manager.getAppiumService().getUrl().toString();                AndroidExecutorFactory executorFactory = new AndroidExecutorFactory();                executor = executorFactory.getHGTask(cmd);                executor.init(appiumUrl, cmd);                AppDownloadExecutorHandler appHander = new AppDownloadExecutorHandler();                ScriptDownloadExecutorHandler scriptHander = new ScriptDownloadExecutorHandler();                ExecutorHandler pythonHander = new ExecutorHandler();                appHander.setHandler(scriptHander);                scriptHander.setHandler(pythonHander);                appHander.handleRequest(executor);                grpcClientService.missionComplete(cmd.getExeId(), cmd.getDeviceId());            } catch (Exception e){                log.error("{} 任务执行错误", cmd.getDeviceId(), e);                grpcClientService.gameover(cmd.getExeId(), cmd.getDeviceId(), e.getMessage());            } finally {                grpcClientService.appiumLogUpload(cmd.getExeId(), cmd.getDeviceId(), appiumLogPath);            }        }else{            log.error("Appium 启动失败");            grpcClientService.gameover(cmd.getExeId(), cmd.getDeviceId(), "Appium 启动失败");        }        pool.release(manager);    }    public void kill() {        log.info("设备 {} 测试任务被停止", cmd.getDeviceId());        if (executor != null) {            executor.stop();        }        if(pool != null){            pool.release(manager);        }    }}