package com.testwa.distest.client.device.driver;

import com.testwa.distest.client.device.manager.DeviceInitException;
import io.rpc.testwa.device.DeviceType;

/**
 * 远程控制驱动接口
 * Created by wen on 2019/5/23
 **/
public interface IDeviceRemoteControlDriver {

    /**
     * 初始化第一步调用， 包含注册
     */
    void deviceInit() throws DeviceInitException;

    /**
     * 设备注册，断开连接之后提供再次注册设备的方法
     */
    void register();

    /**
     * 是否在线
     * @return
     */
    boolean isOnline();

    /**
     * 屏幕和操作同步
     */
    void startProjection(String command);

    /**
     * 屏幕同步暂停 同 stop
     */
//    void waitProjection();

    /**
     * 屏幕同步唤醒 同 start
     */
//    void notifyProjection();

    /**
     * 屏幕同步关闭
     */
    void stopProjection();

    /**
     * 屏幕帧率
     */
    void rate(String command);

    /**
     * 日志
     */
    void startLog(String command);

    void waitLog();

    void notifyLog();

    void stopLog();

    /**
     * 录制
     */
    void startRecorder();

    void stopRecorder();

    /**
     * 是否真的离线
     * @return
     */
    boolean isRealOffline();

    void debugStart();

    void debugStop();

    /*==========================操作相关接口===============================*/
    /**
     * 按钮事件
     * @param keyCode
     */
    void keyevent(int keyCode);

    void inputText(String cmd);

    void touch(String cmd);
    void swip(String cmd);
    void tap(String cmd);
    void tapAndHold(String cmd);

    void setRotation(String cmd);

    void installApp(String command);

    void uninstallApp(String command);

    void openWeb(String cmd);


    /*==========================任务相关接口===============================*/
    /**
     * 启动遍历测试任务
     * @param command
     */
    void startCrawlerTask(String command);

    /**
     * 启动兼容测试任务
     * @param command
     */
    void startCompatibilityTask(String command);

    /**
     * 启动回归测试任务
     * @param command
     */
    void startFunctionalTask(String command);

    /**
     * 停止任务
     */
    void stopTask(String taskCode);

    /*==========================基本信息相关接口===============================*/
    /**
     * 获取设备类型
     * @return
     */
    DeviceType getType();

    String getDeviceId();

    void destory();
}
