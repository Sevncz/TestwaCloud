package com.testwa.distest.client.executor;import com.testwa.core.cmd.RemoteRunCommand;import net.sf.cglib.proxy.Enhancer;public class ProxyFactory {    public static <T> T getPyExecutorInstance(Class clazz, RemoteRunCommand cmd) {        Enhancer enhancer = new Enhancer();        enhancer.setSuperclass(clazz);        enhancer.setCallback(new ExecutorCglibProxy(cmd));        return (T) enhancer.create();    }    public static <T> T getInstance(Class clazz) {        Enhancer enhancer = new Enhancer();        enhancer.setSuperclass(clazz);        enhancer.setCallback(new CglibProxy());        return (T) enhancer.create();    }}