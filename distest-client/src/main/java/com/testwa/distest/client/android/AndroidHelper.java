package com.testwa.distest.client.android;

import com.android.ddmlib.*;
import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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

    public TreeSet<AndroidDevice> getAllDevices() {
        return adbs.getDevices();
    }


    public AndroidDevice getFirstDevice() {
        TreeSet<AndroidDevice> devices = getAllDevices();
        if (!devices.isEmpty()) {
            return devices.first();
        }
        return null;
    }

    public AndroidDevice getAndroidDevice(String serial) {
        try {
            return adbs.getDeviceBySerial(serial);
        }catch (Exception e){
        }
        return null;
    }


    public void installApp(String appPath, String deviceId) {
        if (StringUtils.isBlank(appPath) || StringUtils.isBlank(deviceId)) {
            return;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if (device != null) {
            AndroidApp app = new DefaultAndroidApp(new File(appPath));
            device.install(app);
        }
    }


    public void unInstallApp(String appPath, String deviceId) {
        if (StringUtils.isBlank(appPath) || StringUtils.isBlank(deviceId)) {
            return;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if (device != null) {
            AndroidApp app = new DefaultAndroidApp(new File(appPath));
            if (device.isInstalled(app)) {
                device.uninstall(app);
            }
        }
    }

    public Client[] getAllClient(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return null;
        }
        AndroidDevice device = adbs.getDeviceBySerial(deviceId);
        if (device != null) {
            return device.getAllClient();
        }

        return null;
    }


    public String executeShellCommand(IDevice device, String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();

        try {
            device.executeShellCommand(command, output);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return output.getOutput();
    }

    /**
     * TODO: 添加自定义adb命令，原因是安卓手表的传输速度太慢，导致adb push超时错误
     *
     * @param device
     * @param command
     * @return
     */
    public static String executeCommand(IDevice device, String command) {
        return "";
    }

    private ListenableFuture<List<AdbForward>> executeGetForwardList() {
        final File adbFile = new File(AndroidSdk.adb().getAbsolutePath());
        final SettableFuture future = SettableFuture.create();
        (new Thread(new Runnable() {
            public void run() {
                ProcessBuilder pb = new ProcessBuilder(new String[]{adbFile.getPath(), "forward", "--list"});
                pb.redirectErrorStream(true);
                Process p = null;

                try {
                    p = pb.start();
                } catch (IOException e) {
                    future.setException(e);
                    return;
                }

                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                try {
                    String line;
                    try {
                        List<AdbForward> list = new ArrayList<AdbForward>();
                        while ((line = br.readLine()) != null) {
                            //64b2b4d9 tcp:555 localabstract:shit
                            AdbForward forward = new AdbForward(line);
                            if (forward.isForward()) {
                                list.add(forward);
                            }
                        }
                        future.set(list);
                        return;
                    } catch (IOException ex) {
                        future.setException(ex);
                        return;
                    }
                } finally {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        future.setException(ex);
                    }

                }
            }
        }, "Obtaining adb version")).start();
        return future;
    }

    public AdbForward[] getForwardList() {
        ListenableFuture<List<AdbForward>> future = executeGetForwardList();
        try {
            List<AdbForward> s = future.get(1, TimeUnit.SECONDS);
            AdbForward[] ret = new AdbForward[s.size()];
            s.toArray(ret);
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new AdbForward[0];
    }

}
