package com.testwa.distest.client.component.executor.worker;import com.testwa.core.cmd.RemoteRunCommand;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-15 14:46 **/public interface ExecutorFactory {    FunctionalAbstractExecutor getFunctionalPythonTask(RemoteRunCommand cmd);    CompatibilityAbstractExecutor getCompatibilityAndroidTask(RemoteRunCommand cmd);    CrawlerAbstractExecutor getCrawlerTask(RemoteRunCommand cmd);}