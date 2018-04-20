package com.testwa.distest.server.websocket.handler;import com.alibaba.fastjson.JSON;import com.corundumstudio.socketio.AckRequest;import com.corundumstudio.socketio.SocketIOClient;import com.corundumstudio.socketio.annotation.OnEvent;import com.testwa.core.base.exception.ObjectNotExistsException;import com.testwa.core.cmd.MiniCmd;import com.testwa.core.common.enums.Command;import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;import com.testwa.distest.server.service.device.service.DeviceService;import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;import com.testwa.distest.server.websocket.service.PushCmdService;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.util.HashMap;import java.util.Map;/** * @Program: distest * @Description: 客户端remote client ws的请求处理 * @Author: wen * @Create: 2018-04-20 10:42 **/@Slf4j@Componentpublic class RemoteClientWSHandler {    private final static String login = "login";    @Autowired    private DeviceSessionMgr deviceSessionMgr;    @Autowired    private PushCmdService pushCmdService;    @Autowired    private DeviceAuthMgr deviceAuthMgr;    @Autowired    private DeviceService deviceService;    @Autowired    private SubscribeDeviceFuncMgr subscribeMgr;    @OnEvent(value = login)    public void login(SocketIOClient client, String data, AckRequest ackRequest){        Map j = JSON.parseObject(data, Map.class);        String serial = (String) j.get("sn");        Map<String, String> params = new HashMap<>();        params.put("sn", serial);        params.put("key", serial);        // 设备连接        client.sendEvent(Command.Schem.WAIT.getSchemString(), JSON.toJSONString(params));        deviceSessionMgr.login(serial, client.getSessionId().toString());        deviceAuthMgr.online(serial);        requestStartMinitouch(serial);        requestStartMinicap(serial);        requestStartStfService(serial);    }    private void requestStartMinitouch(String deviceId) throws ObjectNotExistsException {        MiniCmd cmd = new MiniCmd();        cmd.setType("minitouch");        pushCmdService.pushMinCmdStart(cmd, deviceId);    }    private void requestStartMinicap(String deviceId) throws ObjectNotExistsException {        Map<String, Object> config = new HashMap<>();//        config.put("rotate", 0.0f);        config.put("scale", 0.25f);        MiniCmd cmd = new MiniCmd();        cmd.setType("minicap");        cmd.setConfig(config);        pushCmdService.pushMinCmdStart(cmd, deviceId);    }    private void requestStartStfService(String deviceId) throws ObjectNotExistsException {        MiniCmd cmd = new MiniCmd();        cmd.setType("stfagent");        pushCmdService.pushStfAgentCmdStart(cmd, deviceId);    }}