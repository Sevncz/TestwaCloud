package com.testwa.distest.client.control.client.task;import com.testwa.distest.client.exception.DownloadFailException;import java.io.IOException;public abstract class AbstractExecutorHandler {    private AbstractExecutorHandler handler;    public abstract void handleRequest(Executor executor) throws DownloadFailException, IOException;    public AbstractExecutorHandler getHandler() {        return handler;    }    public void setHandler(AbstractExecutorHandler handler) {        this.handler = handler;    }}