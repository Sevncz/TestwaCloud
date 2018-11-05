package com.testwa.distest.client.component.executor.task;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.component.executor.worker.AbstractExecutor;import com.testwa.distest.client.component.executor.worker.AndroidExecutorFactory;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;@Slf4jpublic class CompatibilityAndroidTestTask extends AbstractTestTask{    private AbstractExecutor executor;    private TestTaskListener listener;    public CompatibilityAndroidTestTask(RemoteRunCommand cmd, TestTaskListener listener){        this.cmd = cmd;        this.listener = listener;    }    @Override    public void start() {        log.info("设备 {} 开始兼容测试任务", cmd.getDeviceId());        GrpcClientService grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        try {            AndroidExecutorFactory executorFactory = new AndroidExecutorFactory();            executor = executorFactory.getCompatibilityAndroidTask(cmd);            executor.init(cmd, listener);            executor.start();        } catch (Exception e){            log.error("{} 任务执行错误", cmd.getDeviceId(), e);            grpcClientService.gameover(cmd.getTaskCode(), cmd.getDeviceId(), e.getMessage());        } finally {            log.info("设备 {} 兼容测试任务执行完成", cmd.getDeviceId());        }    }    @Override    public void terminate() {        log.info("设备 {} 测试任务被停止", cmd.getDeviceId());        if (executor != null) {            executor.stop();        }    }}