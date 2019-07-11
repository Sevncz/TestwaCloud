package com.testwa.distest.client.device.screen;

import com.testwa.distest.client.android.ADBTools;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.stfagent.DevDisplay;
import com.testwa.distest.client.util.CommandLineExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;


@Slf4j
public class Capture {

    public static String androidScreenCapture(DevDisplay devDisplay) {
        String captureFile = String.format("/data/local/tmp/minicap_%s.jpg", System.currentTimeMillis());
        String[] shellCommand = ADBTools.buildAdbShell(devDisplay.getDevSerial());
        String command = String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s -s >%s", projectionFormat(devDisplay), captureFile);
        String[] mainCommand =  ArrayUtils.addAll(shellCommand, command);
        CommandLineExecutor.execute(mainCommand);
        // adb pull 到本地
        String localFile = Constant.AGENT_TMP_DIR + File.separator + devDisplay.getDevSerial() + ".jpg";
        boolean success = ADBTools.pullFile(devDisplay.getDevSerial(), captureFile, localFile);
        if(success) {
            ADBTools.deleteFile(devDisplay.getDevSerial(), captureFile);
        }
        return localFile;
    }

    private static String projectionFormat(DevDisplay devDisplay) {
        return String.format("%sx%s@%sx%s/%d", devDisplay.getScreenWidth(), devDisplay.getScreenHeight(), devDisplay.getScreenWidth(), devDisplay.getScreenHeight(), devDisplay.getRotation());
    }

}
