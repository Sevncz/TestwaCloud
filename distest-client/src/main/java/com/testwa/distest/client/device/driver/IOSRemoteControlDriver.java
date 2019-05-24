package com.testwa.distest.client.device.driver;

import com.testwa.distest.client.ios.IOSDeviceUtil;
import io.rpc.testwa.device.DeviceType;

/**
 * iOS设备驱动
 *
 * @author wen
 * @create 2019-05-23 18:52
 */
public class IOSRemoteControlDriver implements IDeviceRemoteControlDriver {
    private final static DeviceType TYPE = DeviceType.IOS;
    private final String udid;

    public IOSRemoteControlDriver(IDeviceRemoteControlDriverCapabilities capabilities) {
        this.udid = capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID);
    }

    @Override
    public void deviceInit() {

    }

    @Override
    public void register() {

    }

    @Override
    public boolean isOnline() {
        return IOSDeviceUtil.isOnline(udid);
    }

    @Override
    public void startScreen(String command) {

    }

    @Override
    public void waitScreen() {

    }

    @Override
    public void notifyScreen() {

    }

    @Override
    public void stopScreen() {

    }

    @Override
    public void startLog(String command) {

    }

    @Override
    public void waitLog() {

    }

    @Override
    public void notifyLog() {

    }

    @Override
    public void stopLog() {

    }

    @Override
    public void startRecorder() {

    }

    @Override
    public void stopRecorder() {

    }

    @Override
    public DeviceType getType() {
        return TYPE;
    }

    @Override
    public String getDeviceId() {
        return null;
    }

    @Override
    public boolean isRealOffline() {
        return false;
    }

    @Override
    public void debugStart() {

    }

    @Override
    public void debugStop() {

    }

    @Override
    public void keyevent(int keyCode) {

    }

    @Override
    public void inputText(String cmd) {

    }

    @Override
    public void touch(String cmd) {

    }

    @Override
    public void installApp(String command) {

    }

    @Override
    public void uninstallApp(String command) {

    }

    @Override
    public void openWeb(String cmd) {

    }

    @Override
    public void startCrawlerTask(String command) {

    }

    @Override
    public void startCompatibilityTask(String command) {

    }

    @Override
    public void startFunctionalTask(String command) {

    }

    @Override
    public void stopTask(String taskCode) {

    }
}
