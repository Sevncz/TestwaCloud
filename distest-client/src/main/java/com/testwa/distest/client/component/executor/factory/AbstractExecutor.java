package com.testwa.distest.client.component.executor.factory;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.exception.DownloadFailException;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-15 14:28 **/public abstract class AbstractExecutor {    public abstract void init(RemoteRunCommand cmd);    public abstract void downloadApp() throws DownloadFailException, IOException;    public abstract void start();    public abstract void stop();}