package com.testwa.distest.server.websocket.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.KeyCode;
import com.testwa.core.common.enums.Command;
import com.testwa.core.cmd.MiniCmd;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class PushCmdService {

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private ClientSessionMgr clientSessionMgr;

    private final SocketIOServer server;

    @Autowired
    public PushCmdService(SocketIOServer server) {
        this.server = server;
    }

    private SocketIOClient getDeviceClientSocketIOClient(String deviceId) {
        String sessionId = deviceSessionMgr.getDeviceSession(deviceId);
        if(StringUtils.isEmpty(sessionId)){
            log.error("device {} session not found", deviceId);
            return null;
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("device {} SocketIOClient not found", deviceId);
            return null;
        }
        return client;
    }
    private SocketIOClient getMainClientSocketIOClient(Long userId) {
        String sessionId = clientSessionMgr.getClientSession(userId);
        if(StringUtils.isEmpty(sessionId)){
            log.error("agent {} session not found", userId);
            return null;
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("agent {} SocketIOClient not found", userId);
            return null;
        }
        return client;
    }

    /**
     *@Description: 启动mincap和minitouch组件
     *@Param: [cmd, deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushMinCmdStart(MiniCmd cmd, String deviceId) {
        log.info(cmd.toString());
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

    /**
     *@Description: 启动stfagent组件
     *@Param: [cmd, deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushStfAgentCmdStart(MiniCmd cmd, String deviceId) {
        log.info(cmd.toString());
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

    /**
     *@Description: 发送触摸数据
     *@Param: [deviceId, data]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushTouchData(String deviceId, String data) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.TOUCH.getSchemString(), data);
    }

    /**
     *@Description: 模拟文字输入
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushInputText(String deviceId, String data) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.INPUT.getSchemString(), data);
    }
    /**
     *@Description: 模拟Home键
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushHome(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.HOME.getSchemString(), "");
    }
    /**
     *@Description: 模拟返回键
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushBack(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.BACK.getSchemString(), "");
    }
    /**
     *@Description: 模拟菜单键
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushMenu(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.MENU.getSchemString(), "");
    }
    /**
     *@Description: 模拟删除键
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushDel(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.KEYEVENT.getSchemString(), KeyCode.KEYCODE_DEL+"");
    }

    /**
     *@Description: 安装并启动minicap和minitouch
     *@Param: [userId, deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void pushInitDeviceClient(Long userId, String deviceId) {
        SocketIOClient client = getMainClientSocketIOClient(userId);
        if(client != null)
        client.sendEvent(WebsocketEvent.ON_START, deviceId);
    }

    /**
     *@Description: 屏幕截图上传开始
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    public void pushScreenUploadStart(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.WAITTING.getSchemString(), "");
    }

    /**
     *@Description: 屏幕截图上传停止
     *@Param: [deviceId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    public void pushScreenUploadStop(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.WAIT.getSchemString(), "");
    }

    /**
     *@Description: 设备发起执行任务命令
     *@Param: [cmd, userId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    @Async
    public void executeCmd(RemoteRunCommand cmd, Long userId) {
        log.info(cmd.toString());
        SocketIOClient client = getDeviceClientSocketIOClient(cmd.getDeviceId());
        if(client != null)
            client.sendEvent(Command.Schem.START_TASK.getSchemString(), JSON.toJSONString(cmd));
    }

    /**
     *@Description: 通知设备开始上传logcat
     *@Param: [deviceId, content]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    public void pushLogcatUploadStart(String deviceId, String content) {
        Map<String, String> cmd = new HashMap<>();
        cmd.put("content", content);
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.START_LOGCAT.getSchemString(), JSON.toJSONString(cmd));
    }

    /**
     *@Description: 通知设备停止上传logcat
     *@Param: [deviceId, content]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    public void pushLogcatUploadStop(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.WAIT_LOGCAT.getSchemString(), "");
    }

    /**
     *@Description: 指定设备安装指定app
     *@Param: [deviceId, appId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/9
     */
    public void pushInstallApp(String deviceId, AppInfo app) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.INSTALL.getSchemString(), JSON.toJSONString(app));

    }

    public void pushUninstallApp(String deviceId, String appBasePackage) {
        Map<String, String> cmd = new HashMap<>();
        cmd.put("appBasePackage", appBasePackage);
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.UNINSTALL.getSchemString(), JSON.toJSONString(cmd));
    }

    /**
     *@Description: 通知设备执行shell命令
     *@Param: [deviceId, cmd]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/10
     */
    public void pushShell(String deviceId, String cmd) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.SHELL.getSchemString(), cmd);
    }

    /**
     *@Description: 使用浏览器打开url
     *@Param: [deviceId, url]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/10
     */
    public void pushOpenWeb(String deviceId, String url) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
            client.sendEvent(Command.Schem.OPENWEB.getSchemString(), url);
    }
}
