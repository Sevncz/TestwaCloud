package com.testwa.distest.client.component.executor.factory;import com.testwa.core.cmd.RemoteRunCommand;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-15 14:48 **/public class IOSExecutorFactory implements ExecutorFactory {    @Override    public HGAbstractExecutor getHGTask(RemoteRunCommand cmd) {        return null;    }    @Override    public JRAbstractExecutor getJRTask(RemoteRunCommand cmd) {        return null;    }}