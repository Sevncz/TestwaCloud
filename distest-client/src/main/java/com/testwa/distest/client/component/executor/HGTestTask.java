package com.testwa.distest.client.component.executor;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.component.appium.manager.AppiumManager;import com.testwa.distest.client.component.appium.pool.AppiumManagerPool;import com.testwa.distest.client.component.executor.worker.AndroidExecutorFactory;import com.testwa.distest.client.component.executor.worker.HGAbstractExecutor;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;@Slf4jpublic class HGTestTask extends AbstractTestTask{    private RemoteRunCommand cmd;    private HGAbstractExecutor executor;    private AppiumManagerPool pool;    private AppiumManager manager;    private TestTaskListener listener;    public HGTestTask(RemoteRunCommand cmd, TestTaskListener listener){        this.cmd = cmd;        this.listener = listener;    }    public void start() {        pool = (AppiumManagerPool) ApplicationContextUtil.getBean("customAppiumManagerPool");        GrpcClientService grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        manager = pool.getManager();        String appiumLogPath = manager.getAppiumlogPath();        if(manager.getAppiumService().isRunning()){            try {                String appiumUrl = manager.getAppiumService().getUrl().toString();                AndroidExecutorFactory executorFactory = new AndroidExecutorFactory();                executor = executorFactory.getHGTask(cmd);                executor.init(appiumUrl, cmd, listener);                AppDownloadExecutorHandler appHander = new AppDownloadExecutorHandler();                ScriptDownloadExecutorHandler scriptHander = new ScriptDownloadExecutorHandler();                ExecutorHandler pythonHander = new ExecutorHandler();                appHander.setHandler(scriptHander);                scriptHander.setHandler(pythonHander);                appHander.handleRequest(executor);                grpcClientService.missionComplete(cmd.getTaskCode(), cmd.getDeviceId());            } catch (Exception e){                log.error("{} 任务执行错误", cmd.getDeviceId(), e);                grpcClientService.gameover(cmd.getTaskCode(), cmd.getDeviceId(), e.getMessage());            } finally {                grpcClientService.appiumLogUpload(cmd.getTaskCode(), cmd.getDeviceId(), appiumLogPath);            }        }else{            log.error("Appium 启动失败");            grpcClientService.gameover(cmd.getTaskCode(), cmd.getDeviceId(), "Appium 启动失败");        }        pool.release(manager);        listener.taskFinish();    }    public void kill() {        log.info("设备 {} 测试任务被停止", cmd.getDeviceId());        if (executor != null) {            executor.stop();        }        if(pool != null){            pool.release(manager);        }    }}