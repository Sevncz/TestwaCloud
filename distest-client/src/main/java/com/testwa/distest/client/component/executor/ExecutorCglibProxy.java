package com.testwa.distest.client.component.executor;import com.alibaba.fastjson.JSON;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.event.ExecutorLogNotifyEvent;import com.testwa.distest.client.model.ExecutorLogInfo;import lombok.extern.slf4j.Slf4j;import net.sf.cglib.proxy.MethodInterceptor;import net.sf.cglib.proxy.MethodProxy;import org.springframework.context.ApplicationContext;import java.lang.reflect.Method;import java.util.Arrays;@Slf4jpublic class ExecutorCglibProxy implements MethodInterceptor {    private RemoteRunCommand cmd;    public ExecutorCglibProxy(RemoteRunCommand cmd){        this.cmd = cmd;    }    @Override    public Object intercept(Object proxy, Method method, Object[] args,                            MethodProxy invocation) throws Throwable {        // 日志开始        log.debug("the method [" + method.getName() + "]"                + "begin with args (" + Arrays.toString(args) + ")");        ExecutorActionInfo actionInfo = method.getAnnotation(ExecutorActionInfo.class);        String desc = "";        int order = -1;        if(actionInfo != null){            desc = actionInfo.desc();            order = actionInfo.order();            methodStartLogToServer(cmd, desc, order, method.getName(), args);        }        Object result = invocation.invokeSuper(proxy, args);        if(actionInfo != null){            methodEndLogToServer(cmd, desc, order, method.getName(), args);        }        // 日志结束        log.debug("the method [" + method.getName() + "]"                + "end with result (" + result + ")");        return result;    }    private void methodStartLogToServer(RemoteRunCommand cmd, String action, int order, String methodName, Object[] args) {        log.info("{} - {}", cmd.getDeviceId(), action);        ApplicationContext context = ApplicationContextUtil.getApplicationContext();        ExecutorLogInfo logInfo = new ExecutorLogInfo();        logInfo.setAction(action);        logInfo.setArgs(JSON.toJSONString(args));        logInfo.setDeviceId(cmd.getDeviceId());        logInfo.setFlag("runOneScript");        logInfo.setMethodName(methodName);        logInfo.setTaskId(cmd.getExeId());        logInfo.setOrder(order);        logInfo.setTime(TimeUtil.getTimestampLong());        context.publishEvent(new ExecutorLogNotifyEvent(this, logInfo));    }    private void methodEndLogToServer(RemoteRunCommand cmd, String action, int order, String methodName, Object[] args) {        ApplicationContext context = ApplicationContextUtil.getApplicationContext();        ExecutorLogInfo logInfo = new ExecutorLogInfo();        logInfo.setAction(action);        logInfo.setArgs(JSON.toJSONString(args));        logInfo.setDeviceId(cmd.getDeviceId());        logInfo.setFlag("end");        logInfo.setMethodName(methodName);        logInfo.setTaskId(cmd.getExeId());        logInfo.setOrder(order);        logInfo.setTime(TimeUtil.getTimestampLong());        context.publishEvent(new ExecutorLogNotifyEvent(this, logInfo));    }}