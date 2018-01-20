package com.testwa.distest.common.android;

import com.android.ddmlib.Client;
import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.image.ImageUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Created by wen on 16/8/13.
 */
public class AndroidHelper {
    private volatile static AndroidHelper ah;
    private AndroidDeviceStore adbs = null;

    private AndroidHelper() {
        adbs = AndroidDeviceStore.getInstance();
    }

    public static AndroidHelper getInstance() {
        if (ah == null) {
            synchronized (AndroidHelper.class) {
                if (ah == null) {
                    ah = new AndroidHelper();
                }
            }
        }
        return ah;
    }

    public TreeSet<AndroidDevice> getAllDevices(){
        return adbs.getDevices();
    }

    public AndroidDevice getAndroidDevice(String serial){
        return adbs.getDeviceBySerial(serial);
    }

    public String screenshot(String deviceId, String filename){
        if(StringUtils.isBlank(deviceId) ){
            return null;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if(device != null) {
            BufferedImage image = device.takeScreenshot();
            if (StringUtils.isBlank(filename)) {
                filename = "screenshot.png";
            }
            String imagePath = new File(System.getProperty("java.io.tmpdir"),
                    filename).getAbsolutePath();
            ImageUtils.writeToFile(image, imagePath);
            return imagePath;
        }
        return null;

    }

    public byte[] screenshotByte(String deviceId) throws IOException {
        if(StringUtils.isBlank(deviceId) ){
            return null;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if(device != null) {
            BufferedImage image = device.takeScreenshot();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            byte[] b = out.toByteArray();
            return b;
        }
        return null;

    }

    public void installApp(String appPath, String deviceId){
        if(StringUtils.isBlank(appPath) || StringUtils.isBlank(deviceId) ){
            return;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if(device != null){
            AndroidApp app = new DefaultAndroidApp(new File(appPath));
            device.install(app);
        }
    }


    public void unInstallApp(String appPath, String deviceId){
        if(StringUtils.isBlank(appPath) || StringUtils.isBlank(deviceId) ){
            return;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if(device != null) {
            AndroidApp app = new DefaultAndroidApp(new File(appPath));
            if (device.isInstalled(app)) {
                device.uninstall(app);
            }
        }
    }

    public Client[] getAllClient(String deviceId){
        if(StringUtils.isBlank(deviceId) ){
            return null;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if(device != null) {
            return device.getAllClient();
        }

        return null;
    }


}
