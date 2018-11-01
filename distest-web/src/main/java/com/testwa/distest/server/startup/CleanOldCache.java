package com.testwa.distest.server.startup;import com.testwa.distest.server.service.cache.mgr.WebsocketLoginMgr;import com.testwa.distest.server.service.cache.mgr.DeviceLoginMgr;import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.CommandLineRunner;import org.springframework.stereotype.Component;@Slf4j@Componentpublic class CleanOldCache implements CommandLineRunner {    @Autowired    private SubscribeDeviceFuncMgr subscribeMgr;    @Autowired    private DeviceLoginMgr deviceSessionMgr;    @Autowired    private WebsocketLoginMgr clientSessionMgr;    @Autowired    private DeviceOnlineMgr deviceOnlineMgr;    @Override    public void run(String... strings) {        subscribeMgr.delAllSubscribes();        deviceSessionMgr.delAllDeviceSessions();        clientSessionMgr.delAllClientSessions();        deviceOnlineMgr.delAllOnline();    }}