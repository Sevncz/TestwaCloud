package com.testwa.distest.client.component.minicap;

import com.android.ddmlib.*;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.MinicapServiceBuilder;
import com.testwa.core.utils.Identities;

import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.component.port.MinicapPortProvider;
import com.testwa.distest.client.component.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by wen on 2017/4/17.
 */
@Slf4j
public class AMinicapServer extends MinicapServer{
    private IDevice device;
    private String resourcesPath;
    // 物理屏幕宽高
    private Size deviceSize;
    private Integer port;
    private float scale;
    private int rotate;

    private static String BIN = "";
    private static String MINICAP_CHMOD_COMMAND = "chmod 777 %s/%s";
    private static String MINICAP_WM_SIZE_COMMAND = "wm size";
    private static String MINICAP_DIR_COMMAND = String.format("mkdir %s 2>/dev/null || true", Constant.MINICAP_DIR);
    private static String LOCALABSTRACT = "MINI.CAP";

    private AdbDriverService service;

    public AMinicapServer(String serialNumber, String resourcesPath, final float scale, final int rotate) {
        this.resourcesPath = resourcesPath;
        this.scale = scale;
        this.rotate = rotate;
        int install = 5;
        AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(serialNumber);
        while (install > 0) {
            try {
                this.device = ad.getDevice();
                installMinicap(device, resourcesPath);
                break;
            } catch (MinicapInstallException | DeviceNotFoundException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e1) {

                }
                install--;
            }
        }
        // 获取设备屏幕的实际尺寸
        String output = ad.runAdbCommand("shell " + MINICAP_WM_SIZE_COMMAND);
        if (output != null && !output.isEmpty()) {
            String overrideSizeFlag = "Override";
            if(output.contains(overrideSizeFlag)){
                output = output.split("\n")[1].trim();
            }
            String sizeStr = output.split(":")[1].trim();
            int screenWidth = Integer.parseInt(sizeStr.split("x")[0].trim());
            int screenHeight = Integer.parseInt(sizeStr.split("x")[1].trim());
            deviceSize = new Size(screenWidth, screenHeight);
        }


    }

    /**
     * 将minicap的二进制和.so文件push到/data/local/tmp文件夹下，启动minicap服务
     */
    private static void installMinicap(IDevice device, String resourcesPath) throws MinicapInstallException {

        String abi = device.getProperty(Constant.PROP_ABI);
        String sdk = device.getProperty(Constant.PROP_SDK);
        String rel = device.getProperty(Constant.PROP_REL);


        if (StringUtils.isEmpty(sdk) || StringUtils.isEmpty(abi)) {
            throw new MinicapInstallException("cant not get device info. please check device is connected");
        }

        Integer sdkvalue = Integer.parseInt(sdk);
        if (sdkvalue >= 16) {
            BIN = Constant.MINICAP_BIN;
        } else {
            BIN = Constant.MINICAP_NOPIE;
        }

        String minicapBinPath = resourcesPath + File.separator + Constant.getMinicapBin() + File.separator + abi + File.separator + BIN;
        String minicapSoPath = resourcesPath + File.separator + Constant.getMinicapSo() + File.separator + "android-" + sdk + File.separator + abi + File.separator + Constant.MINICAP_SO;

        // Create a directory for minicap resources
        AndroidHelper.getInstance().executeShellCommand(device, MINICAP_DIR_COMMAND);

        try {
            // 将minicap的可执行文件和.so文件一起push到设备中
            String minicapCheckOut = AndroidHelper.getInstance().executeShellCommand(device, String.format("ls %s | grep %s", Constant.MINICAP_DIR, BIN));
            // Upload the binary
            if (!minicapCheckOut.contains(BIN)) {
                device.pushFile(minicapBinPath, Constant.MINICAP_DIR + "/" + BIN);
            }
            // Upload the shared library
            if (!minicapCheckOut.contains(Constant.MINICAP_SO)) {
                device.pushFile(minicapSoPath, Constant.MINICAP_DIR + "/" + Constant.MINICAP_SO);
            }
            AndroidHelper.getInstance().executeShellCommand(device, String.format(MINICAP_CHMOD_COMMAND, Constant.MINICAP_DIR, BIN));
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }
    }


    public void start(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String[] args) {

        if(StringUtils.isEmpty(BIN)){
            try {
                installMinicap(device, resourcesPath);
            } catch (MinicapInstallException e) {
                e.printStackTrace();
            }
        }

        if(this.port == null) {
            this.port = MinicapPortProvider.pullPort();
        }
        ADBCommandUtils.forwardAbstract(device.getSerialNumber(), this.port, this.LOCALABSTRACT);
        
        service = new MinicapServiceBuilder()
                .whithBin(BIN)
                .whithDeviceId(device.getSerialNumber())
                .whithLibPath(Constant.MINICAP_DIR)
                .whithSize(String.format("%dx%d@%dx%d/%d", ow, oh, dw, dh, rotate))
                .whithRotate(rotate)
                .whithShipFrame(shipFrame)
                .whithName(LOCALABSTRACT)
                .whithArgs(args)
                .build();
        service.start();
        int tryTime = 20;
        while(tryTime > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
                String out = service.getStdOut();
                if(StringUtils.isNotBlank(out)){
                    log.info(out);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tryTime--;

        }
    }

    @Override
    public void start() {
        start(deviceSize.w, deviceSize.h, (int) (deviceSize.w * scale), (int) (deviceSize.h * scale), rotate, true, null);
    }

    @Override
    public void restart() {
        if (service != null) {
            service.stop();
        }
        start();
    }

    public void restart(final float scale, final int rotate) {
        this.scale = scale;
        this.rotate = rotate;
        restart();
    }

    @Override
    public void stop() {
        if (service != null) {
            service.stop();
        }
    }

    /*
    Usage: /data/local/tmp/minicap [-h] [-n <name>]
      -d <id>:       Display ID. (0)
      -n <name>:     Change the name of the abtract unix domain socket. (minicap)
      -P <value>:    Display projection (<w>x<h>@<w>x<h>/{0|90|180|270}).
      -Q <value>:    JPEG quality (0-100).
      -s:            Take a screenshot and output it to stdout. Needs -P.
      -S:            Skip frames when they cannot be consumed quickly enough.
      -t:            Attempt to get the capture method running, then exit.
      -i:            Get display information in JSON format. May segfault.
     */
    public String getMinicapCommand(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String name, String[] args) {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(String.format("LD_LIBRARY_PATH=%s", Constant.MINICAP_DIR));
        commands.add(Constant.MINICAP_DIR + "/" + BIN);
        commands.add("-P");
        commands.add(String.format("%dx%d@%dx%d/%d", ow, oh, dw, dh, rotate));
        commands.add("-n");
        commands.add(name);
        if (shipFrame)
            commands.add("-S");
        if (args != null) {
            for (String s : args) {
                commands.add(s);
            }
        }
        String command = StringUtils.join(commands, " ");
        log.info("minicap runOneScript command {}", command);
        return command;
    }

    /**
     * 屏幕截图
     * <p>
     * 由于个平台的换行符导致二进制流输出有问题，二进制数据将先base64编码后传输
     *
     * @return
     */
    public byte[] takeScreenShot() {

        String filename = Identities.randomLong() + ".jpg";
        String takeScreenShotCommand = getMinicapCommand(deviceSize.w, deviceSize.h, deviceSize.w, deviceSize.h, 0, false, filename, new String[]{"-s -b"});

        log.info("takeScreenShot:" + takeScreenShotCommand);
        BinaryOutputReceiver receiver = new BinaryOutputReceiver();
        try {
            device.executeShellCommand(takeScreenShotCommand, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // remove text output
        byte[] bytes = receiver.getOutput();
        do {
            String dataStr = new String(bytes);
            int jpgStart = dataStr.indexOf("/9j/");

            if (jpgStart >= 0) {
                dataStr = dataStr.substring(jpgStart); // jpg特征码base64
            } else {
                break;
            }

            try {
                bytes = new BASE64Decoder().decodeBuffer(dataStr);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (bytes[0] != -1 && bytes[1] != -40) {
                log.error("not a jpg file!!");
                break;
            }

            return bytes;
        } while (false);

        return new byte[0];
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
