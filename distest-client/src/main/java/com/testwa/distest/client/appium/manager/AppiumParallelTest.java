package com.testwa.distest.client.appium.manager;

import com.testwa.distest.client.appium.ios.IOSDeviceConfiguration;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class AppiumParallelTest{
    private static Logger LOG = LoggerFactory.getLogger(AppiumParallelTest.class);

    public AppiumManager appiumMan = new AppiumManager();
    public String device_udid;
    public String category = null;

    private static IOSDeviceConfiguration iosDevice = new IOSDeviceConfiguration();
    private static AndroidDeviceConfiguration androidDevice = new AndroidDeviceConfiguration();


    public synchronized AppiumDriverLocalService startAppiumServer(String device_udid, String installApp, String testcaseLogId) throws Exception {
        if (device_udid == null) {
            LOG.error("No devices are free to run test or Failed to run test");
            return null;
        }
        this.device_udid = device_udid;
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            if (iosDevice.checkiOSDevice(device_udid)) {
                iosDevice.setIOSWebKitProxyPorts(device_udid);
                category = iosDevice.getDeviceName(device_udid).replace(" ", "_");
            } else if (!iosDevice.checkiOSDevice(device_udid)) {
                category = androidDevice.getDeviceModel(device_udid);

                System.out.println(category);
            }
        } else {
            category = androidDevice.getDeviceModel(device_udid);
        }

        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                if (iosDevice.checkiOSDevice(device_udid)) {
                    String webKitPort = iosDevice.startIOSWebKit(device_udid);
                    return appiumMan.appiumServerForIOS(device_udid, webKitPort);
                } else if (!iosDevice.checkiOSDevice(device_udid)) {
                    return appiumMan.appiumServerForAndroid(device_udid, installApp, testcaseLogId);
                }
            } else {
                return appiumMan.appiumServerForAndroid(device_udid, installApp, testcaseLogId);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public synchronized void killAppiumServer(){
        System.out.println(
            "**************ClosingAppiumSession****************" + Thread.currentThread().getId());
        appiumMan.destroyAppiumNode();
//        if (driver.toString().split(":")[0].trim().equals("IOSDriver")) {
//            iosDevice.destroyIOSWebKitProxy();
//        }

    }

    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public void removeApkFromDevice(String app_package) throws Exception {
        androidDevice.removeApkFromDevices(device_udid, app_package);
    }


    public synchronized DesiredCapabilities androidNative() {
        System.out.println("Setting Android Desired Capabilities:");
        DesiredCapabilities androidCapabilities = new DesiredCapabilities();
        androidCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android");
        androidCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "5.X");
        androidCapabilities.setCapability("browserName", "");
        int android_api = Integer.parseInt(androidDevice.deviceOS(device_udid));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Android API Level::" + android_api);
        if (android_api <= 16) {
            androidCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Selendroid");
        }
//        androidCapabilities
//            .setCapability(MobileCapabilityType.APP, prop.getProperty("ANDROID_APP_PATH"));
//        androidCapabilities
//            .setCapability(MobileCapabilityType.APP_PACKAGE, prop.getProperty("APP_PACKAGE"));
//        androidCapabilities
//            .setCapability(MobileCapabilityType.APP_ACTIVITY, prop.getProperty("APP_ACTIVITY"));
//        if (prop.getProperty("APP_WAIT_ACTIVITY") != null) {
//            androidCapabilities.setCapability(MobileCapabilityType.APP_WAIT_ACTIVITY,
//                prop.getProperty("APP_WAIT_ACTIVITY"));
//        }
        return androidCapabilities;
    }

    public synchronized DesiredCapabilities androidWeb() {
        DesiredCapabilities androidWebCapabilities = new DesiredCapabilities();
        androidWebCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android");
        androidWebCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "5.0.X");
        // If you want the tests on real device, make sure chrome browser is
        // installed
//        androidWebCapabilities
//            .setCapability(MobileCapabilityType.BROWSER_NAME, prop.getProperty("BROWSER_TYPE"));
        androidWebCapabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, true);
        return androidWebCapabilities;
    }

    public synchronized DesiredCapabilities iosNative() {
        DesiredCapabilities iOSCapabilities = new DesiredCapabilities();
        System.out.println("Setting iOS Desired Capabilities:");
        iOSCapabilities.setCapability("platformVersion", "9.0");
//        iOSCapabilities.setCapability("app", prop.getProperty("IOS_APP_PATH"));
//        iOSCapabilities.setCapability("bundleId", prop.getProperty("BUNDLE_ID"));
        iOSCapabilities.setCapability("autoAcceptAlerts", true);
        iOSCapabilities.setCapability("deviceName", "iPhone");
        return iOSCapabilities;
    }

    public void deleteAppIOS(String bundleID) throws InterruptedException, IOException {
        iosDevice.unInstallApp(device_udid, bundleID);
    }

    public void installAppIOS(String appPath) throws InterruptedException, IOException {
        iosDevice.installApp(device_udid, appPath);
    }

    public Boolean checkAppIsInstalled(String bundleID) throws InterruptedException, IOException {
        return iosDevice.checkIfAppIsInstalled(bundleID);
    }




}
