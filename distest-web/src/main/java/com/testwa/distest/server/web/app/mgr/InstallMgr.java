package com.testwa.distest.server.web.app.mgr;import com.alibaba.fastjson.JSON;import com.google.protobuf.ByteString;import com.testwa.core.cmd.AppInfo;import com.testwa.distest.server.entity.App;import com.testwa.distest.server.service.app.form.AppInstallForm;import com.testwa.distest.server.service.app.service.AppService;import com.testwa.distest.server.rpc.cache.CacheUtil;import io.grpc.stub.StreamObserver;import io.rpc.testwa.agent.Message;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.BeanUtils;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;/** * @Program: distest * @Description: 安装和卸载App的控制管理 * @Author: wen * @Create: 2018-04-09 15:44 **/@Slf4j@Componentpublic class InstallMgr {    @Autowired    private AppService appService;    /**     *@Description: 安装App到指定的设备上     *@Param: [appInstallForm]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void install(AppInstallForm form) {        App app = appService.get(form.getAppId());        AppInfo appInfo = new AppInfo();        BeanUtils.copyProperties(app, appInfo);        for (String key : form.getDeviceIds()) {            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(key);            if(observer != null){                Message message = Message.newBuilder().setTopicName(Message.Topic.INSTALL_APP).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(appInfo))).build();                observer.onNext(message);            }else{            }        }    }    /**     *@Description: 从指定设备上卸载该App     *@Param: [appInstallForm]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void uninstall(AppInstallForm form) {        App app = appService.get(form.getAppId());        for (String key : form.getDeviceIds()) {            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(key);            if(observer != null){                Message message = Message.newBuilder().setTopicName(Message.Topic.UNINSTALL_APP).setStatus("OK").setMessage(ByteString.copyFromUtf8(app.getPackageName())).build();                observer.onNext(message);            }else{            }        }    }}