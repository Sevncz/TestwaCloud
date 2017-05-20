package com.testwa.distest.client.android;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import com.github.cosysoft.device.android.AndroidDevice;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * Created by wen on 16/8/13.
 */
public class AndroidHelperUnitTest {
    private static final String deviceId = "ZX1G427G93";

    @Test
    public void testGetAllDevices() {
        TreeSet<AndroidDevice> devices = AndroidHelper.getInstance().getAllDevices();
        for (AndroidDevice device : devices){
            System.out.println(device.getSerialNumber());
        }
        AndroidDevice device = devices.pollFirst();
        System.out.println(device.getName());
    }


    @Test
    public void testScreenshot() {
        String path = AndroidHelper.getInstance().screenshot(deviceId, "");
        System.out.println(path);
    }

    @Test
    public void testInstallApp(){
        AndroidHelper.getInstance().installApp("/Users/wen/Documents/testwa/ContactManager.apk", deviceId);
        Client[] clients = AndroidHelper.getInstance().getAllClient(deviceId);
        for (Client client : clients) {
            ClientData clientData = client.getClientData();
            System.out.println(clientData.getClientDescription() + " " + clientData.getPid());
        }
    }


    @Test
    public void testUninstallApp(){
        AndroidHelper.getInstance().unInstallApp("/Users/wen/Documents/testwa/ContactManager.apk", deviceId);
        Client[] clients = AndroidHelper.getInstance().getAllClient(deviceId);
        for (Client client : clients) {
            ClientData clientData = client.getClientData();
            System.out.println(clientData.getClientDescription() + " " + clientData.getPid());
        }
    }

    @Test
    public void testGetBrand(){

        AndroidDevice device = AndroidHelper.getInstance().getAndroidDevice(deviceId);
        System.out.println("deviceModel - >" + device.runAdbCommand("shell getprop ro.build.version.sdk"));
        System.out.println("deviceModel - >" + device.runAdbCommand("shell getprop ro.product.bluetooth"));
    }

    @Test
    public void testGetApp(){
        TestwaAndroidApp app = new TestwaAndroidApp(new File("/Users/wen/Documents/testwa/ContactManager.apk"));
        System.out.println(app.getVersionName());
        System.out.println("-----");
        System.out.println(app.getAppId());
        System.out.println("-----");
        System.out.println(app.getSdkVersion());
        System.out.println("-----");
        System.out.println(app.getTargetSdkVersion());
    }


    @Test
    public void testLogcat() throws InterruptedException {
        AndroidDevice device = AndroidHelper.getInstance().getAndroidDevice(deviceId);

        LogCatReceiverTask receiverTask = new LogCatReceiverTask(device.getDevice());

        final String tag = "";

        receiverTask.addLogCatListener(new LogCatListener() {
            @Override
            public void log(List<LogCatMessage> list) {
                for (LogCatMessage message : list) {
                    if (tag.equals(message.getTag())) {
                        String str = message.getMessage();
                        try {
                            System.out.print(str);
                        } catch (Exception e) {
                            System.out.println("Failed to parse event - " + str);
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        Thread thread = new Thread(receiverTask);
        thread.start();

    }

}
