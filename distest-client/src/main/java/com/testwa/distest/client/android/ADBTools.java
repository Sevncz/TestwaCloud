package com.testwa.distest.client.android;

import com.testwa.distest.client.exception.CommandFailureException;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import com.testwa.distest.jadb.RemoteFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 使用jadb和process执行adb命令
 * @author wen
 * @create 2019-05-09 11:53
 */
@Slf4j
public class ADBTools {
    public static final String ERROR = "ERROR";

    /**
     * @Description: 阻塞执行shell命令
     * @Param: [deviceId, command]
     * @Return: java.lang.String
     * @Author wen
     * @Date 2019/5/24 10:56
     */
    public static String commandShell(String deviceId, String... command) {
        String[] adbCommand = buildAdbShell(deviceId);
        String[] shellCommand =  ArrayUtils.addAll(adbCommand, command);
        return CommandLineExecutor.execute(shellCommand);
    }

    private static String command(String deviceId, String... command) {
        String[] adbCommand = buildAdb(deviceId);
        String[] notShellCommand =  ArrayUtils.addAll(adbCommand, command);
        return CommandLineExecutor.execute(notShellCommand);
    }

    /**
     * @Description: 异步执行shell命令
     * @Param: [deviceId, command]
     * @Return: java.lang.String
     * @Author wen
     * @Date 2019/5/24 10:56
     */
    public static StartedProcess asyncCommandShell(String deviceId, String... command) {
        String[] adbCommand = buildAdbShell(deviceId);
        String[] shellCommand =  ArrayUtils.addAll(adbCommand, command);
        return CommandLineExecutor.asyncExecute(shellCommand);
    }

    public static String[] buildAdbShell(String deviceId) {
        return new String[]{AndroidSdk.adb().getName(), "-s", deviceId, "shell"};
    }

    public static String[] buildAdb(String deviceId) {
        return new String[]{AndroidSdk.adb().getName(), "-s", deviceId};
    }

    public static boolean pushFile(String deviceId, String localFile, String remoteFile) {
        try {
            command(deviceId, "push", localFile, remoteFile);
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] pushFile 错误", deviceId, e);
        }
        return true;
    }

    public static Boolean chmod(String deviceId, String remoteFile, String mode) {
        try {
            commandShell(deviceId, "chmod", "-R", mode, remoteFile);
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] chmod 错误", deviceId, e);
        }
        return true;
    }

    public static Boolean forward(String deviceId, int port, String socketName) {
        try {
            command(deviceId, "forward", String.format("tcp:%d", port), String.format("localabstract:%s", socketName));
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] forward tcp:{} 错误", deviceId, port, e.getMessage());
        }
        return false;
    }

    public static Boolean forward(String deviceId, int port, int remotePort) {
        try {
            command(deviceId, "forward", String.format("tcp:%d", port), String.format("tcp:%s", remotePort));
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] forward tcp:{} tcp:{} 错误", deviceId, port, remotePort, e.getMessage());
        }
        return false;
    }

    public static Boolean forwardRemove(String deviceId, int port) {
        try {
            command(deviceId, "forward", "--remove", String.format("tcp:%d", port));
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] forward tcp:{} 错误", deviceId, port, e.getMessage());
        }
        return false;
    }

    public static Boolean reverse(String deviceId, String sockName, int port) {
        try {
            command(deviceId, "reverse", String.format("localabstract:%s", sockName), String.format("tcp:%d", port));
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] reverse localabstract:{} tcp:{} 错误", deviceId, sockName, port, e.getMessage());
        }
        return false;
    }

    public static Boolean reverseRemove(String deviceId, String sockName) {
        try {
            command(deviceId, "reverse", "--remove", String.format("localabstract:%s", sockName));
            return true;
        }catch (CommandFailureException e){
            log.error("[{}] remove reverse localabstract:{} 错误", deviceId, sockName);
        }
        return false;
    }

    public static PhysicalSize getPhysicalSize(String deviceId) {
        String ret = commandShell(deviceId, "wm", "size");

        String retLine = ret.split("\n")[0];

        String[] px = StringUtils.substringAfter(retLine, ":").split("x");
        int x = 0;
        int y = 0;
        if(px.length == 2) {
            try {
                x = Integer.parseInt(px[0].trim());
                y = Integer.parseInt(px[1].trim());
            }catch (Exception e) {
                log.error("getPhysicalSize error, [adb -s {} shell wm size] return [{}]", deviceId, ret, e.getMessage());
            }
        }
        return new PhysicalSize(x, y);
    }

    public static String getAbi(String deviceId) {
        return getProp(deviceId, "ro.product.cpu.abi");
    }

    public static String getApi(String deviceId) {
        return getProp(deviceId, "ro.build.version.sdk");
    }

    public static String getProp(String deviceId, String propStr) {
        String result = commandShell(deviceId, "getprop", propStr);
        if(StringUtils.isNoneEmpty(result)) {
            return result.trim();
        }
        return ERROR;
    }

    public static void restartAdb() {
        CommandLineExecutor.execute(new String[]{AndroidSdk.adb().getName(), "kill-server"});
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CommandLineExecutor.execute(new String[]{AndroidSdk.adb().getName(), "start-server"});
    }

    public static String openWeb(String deviceId, String url) {
        String result = commandShell(deviceId, "am", "start", "-a", "android.intent.action.VIEW", "-d", url);
        if(StringUtils.isNoneEmpty(result)) {
            return result.trim();
        }
        return ERROR;
    }


    public static void uninstallApp(String deviceId, String basePackage) {
        command(deviceId, "uninstall", basePackage);
    }

    public static void tcpip(String deviceId, int tcpipPort) {
        command(deviceId,"tcpip", String.valueOf(tcpipPort));
    }

    public static String isTcpip(String deviceId) {
        return commandShell(deviceId,"getprop", "service.adb.tcp.port");
    }

    public static void main(String[] args) {
        String deviceId = "4205dccb";
        String adbResult = ADBTools.getAbi(deviceId);
        System.out.println(adbResult);
    }
}
