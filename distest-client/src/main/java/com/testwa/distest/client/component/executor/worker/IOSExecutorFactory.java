package com.testwa.distest.client.component.executor.worker;import com.testwa.core.cmd.RemoteRunCommand;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-15 14:48 **/public class IOSExecutorFactory implements ExecutorFactory {    @Override    public FunctionalAbstractExecutor getFunctionalPythonTask(RemoteRunCommand cmd) {        return null;    }    @Override    public CompatibilityAbstractExecutor getCompatibilityAndroidTask(RemoteRunCommand cmd) {        return null;    }    @Override    public CrawlerAbstractExecutor getCrawlerTask(RemoteRunCommand cmd) {        return null;    }}