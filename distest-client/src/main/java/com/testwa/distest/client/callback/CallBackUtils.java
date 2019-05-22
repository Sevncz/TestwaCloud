package com.testwa.distest.client.callback;import com.testwa.distest.client.callback.debug.DebugStartCallback;import com.testwa.distest.client.callback.debug.DebugStopCallback;import com.testwa.distest.client.callback.remote.*;import com.testwa.distest.client.callback.task.CrawlerStartCallback;import com.testwa.distest.client.callback.task.JRStartCallback;import com.testwa.distest.client.callback.task.TaskCancelCallback;import com.testwa.distest.client.callback.task.TaskStartCallback;import com.testwa.distest.client.component.stfagent.KeyCode;import com.testwa.distest.client.DeviceClient;import io.rpc.testwa.push.Message;public class CallBackUtils {	@SuppressWarnings("rawtypes")	public static ICallBack getCallBack(Message.Topic topic, DeviceClient client) throws Exception {	    switch (topic) {            case ADB:                return new AdbCallback(client);            case BACK:                return new ButtonEventCallback(client, KeyCode.KEYCODE_BACK);            case HOME:                return new ButtonEventCallback(client, KeyCode.KEYCODE_HOME);            case MENU:                return new ButtonEventCallback(client, KeyCode.KEYCODE_MENU);            case DEL:                return new ButtonEventCallback(client, KeyCode.KEYCODE_DEL);            case INPUT:                return new InputCallback(client);            case OPENWEB:                return new OpenWebCallback(client);            case PUSH_FILE:                return new PushFileCallback(client);            case SHELL:                return new ShellCallback(client);            case TOUCH:                return new MinitouchTouchCallback(client);            case KEY_EVENT:                return new MinitouchKeyEventCallback(client);            case SCREENSHOT:                return new ScreenshotCallback(client);            case TASK_START:                return new TaskStartCallback(client);            case TASK_CANCEL:                return new TaskCancelCallback(client);            case JR_TASK_START:                return new JRStartCallback(client);            case CRAWLER_TASK_START:                return new CrawlerStartCallback(client);            case INSTALL_APP:                return new InstallAppCallback(client);            case UNINSTALL_APP:                return new UninstallAppCallback(client);            case LOGCAT_STOP:                return new LogcatStopCallback(client);            case LOGCAT_WAIT:                return new LogcatStopCallback(client);            case LOGCAT_START:                return new LogcatStartCallback(client);            case COMPONENT_START:                return new ComponentStartCallback(client);            case SCREEN_WAIT:                return new ScreenWaitCallback(client);            case SCREEN_START:                return new ScreenStartCallback(client);            case COMPONENT_STOP:                return new ComponentStopCallback(client);            case DEVICE_INFO://                return new ControlCallback(client);            case MESSAGE://                return new ControlCallback(client);            case UNRECOGNIZED://                return new ControlCallback(client);            case DEBUG_START:                return new DebugStartCallback(client);            case DEBUG_STOP:                return new DebugStopCallback(client);            default:                throw new Exception("Can't find callback for topic: " + topic);        }	}}