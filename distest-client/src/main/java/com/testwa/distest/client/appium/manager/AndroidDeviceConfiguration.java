package com.testwa.distest.client.appium.manager;




import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public class AndroidDeviceConfiguration {

    ArrayList<String> deviceSerail = new ArrayList<String>();

    public ArrayList<String> getDeviceSerial() throws Exception {
        TreeSet<AndroidDevice> ads =  AndroidDeviceStore.getInstance().getDevices();
        for(AndroidDevice device : ads){
            deviceSerail.add(device.getSerialNumber());
        }
        return deviceSerail;
    }

    /*
     * This method gets the device dto name
     */
    public String getDeviceModel(String deviceID) {
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceID);
        String brand = device.runAdbCommand("shell getprop ro.product.brand");
        String deviceModelName = device.runAdbCommand("shell getprop ro.product.dto").replaceAll("\\W", "");
        String deviceModel = deviceModelName.concat("_" + brand);
        return deviceModel.trim();

    }

    /*
     * This method gets the device OS API Level
     */
    public String deviceOS(String deviceID) {
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceID);
        return device.runAdbCommand("shell getprop ro.build.version.sdk");

    }

    /**
     * This method will close the running app
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void closeRunningApp(String deviceID, String app_package)
        throws InterruptedException, IOException {
//         adb -s 192.168.56.101:5555 com.android2.calculator3
//        cmd.runCommand("adb -s " + deviceID + " shell am force-stop " + app_package);
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceID);
        device.runAdbCommand(" shell am force-stop " + app_package);
    }

    /**
     * This method clears the app data only for android
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void clearAppData(String deviceID, String app_package)
        throws InterruptedException, IOException {
        // adb -s 192.168.56.101:5555 com.android2.calculator3
//        cmd.runCommand("adb -s " + deviceID + " shell pm clear " + app_package);
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceID);
        device.runAdbCommand(" shell pm clear " + app_package);
    }

    /**
     * This method removes apk from the devices attached
     *
     * @param app_package
     * @throws Exception
     */

    public void removeApkFromDevices(String deviceID,String app_package) throws Exception {
//            cmd.runCommand("adb -s " + deviceID + " uninstall " + app_package);
        AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceID);
        device.runAdbCommand(" uninstall " + app_package);
    }
}
