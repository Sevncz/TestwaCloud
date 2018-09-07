package com.testwa.distest.client.component.executor;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.component.executor.worker.AbstractExecutor;import com.testwa.distest.client.component.executor.worker.AndroidExecutorFactory;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;@Slf4jpublic class CompatibilityAndroidTestTask extends AbstractTestTask{    private AbstractExecutor executor;    private TestTaskListener listener;    public CompatibilityAndroidTestTask(RemoteRunCommand cmd, TestTaskListener listener){        this.cmd = cmd;        this.listener = listener;    }    public void start() {        log.info("设备 {} 开始测试任务", cmd.getDeviceId());        GrpcClientService grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        try {            AndroidExecutorFactory executorFactory = new AndroidExecutorFactory();            executor = executorFactory.getJRTask(cmd);            executor.init(cmd, listener);            AppDownloadExecutorHandler appHander = new AppDownloadExecutorHandler();            ExecutorHandler executorHandler = new ExecutorHandler();            appHander.setHandler(executorHandler);            appHander.handleRequest(executor);            grpcClientService.missionComplete(cmd.getTaskCode(), cmd.getDeviceId());        } catch (Exception e){            log.error("{} 任务执行错误", cmd.getDeviceId(), e);            grpcClientService.gameover(cmd.getTaskCode(), cmd.getDeviceId(), e.getMessage());        } finally {        }    }    public void kill() {        log.info("设备 {} 测试任务被停止", cmd.getDeviceId());        if (executor != null) {            executor.stop();        }    }}