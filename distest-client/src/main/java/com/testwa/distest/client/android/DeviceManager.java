package com.testwa.distest.client.android;import lombok.extern.slf4j.Slf4j;@Slf4jpublic class DeviceManager {    /*      * 单例      */    private static DeviceManager INSTANCE = null;    /**     * 包装类     */    private AndroidDebugBridgeWrapper androidDebugBridgeWrapper;    /**     * 设备监听器     */    private DeviceChangeListener deviceChangeListener;    /**     * 私有构造函数     */    private DeviceManager() {    }    /**     * 获取单例类     *     * @return DeviceManager     */    public static DeviceManager getInstance() {        if (INSTANCE == null) {            synchronized (DeviceManager.class) {                if (INSTANCE == null) {                    INSTANCE = new DeviceManager();                }            }        }        return INSTANCE;    }    /**     * 启动方法     */    public void start() {        androidDebugBridgeWrapper = new AndroidDebugBridgeWrapper();        deviceChangeListener = new DeviceChangeListener();        androidDebugBridgeWrapper.addDeviceChangeListener(deviceChangeListener);        androidDebugBridgeWrapper.init(false);    }    /**     * 销毁方法     */    public void destory() {        if (androidDebugBridgeWrapper == null) {            return;        }        androidDebugBridgeWrapper.removeDeviceChangeListener(deviceChangeListener);        androidDebugBridgeWrapper.terminate();    }} 