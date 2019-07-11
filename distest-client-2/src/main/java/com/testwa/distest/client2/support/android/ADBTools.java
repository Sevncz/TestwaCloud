package com.testwa.distest.client2.support.android;

import com.testwa.core.shell.UTF8CommonExecs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

/**
 * @author wen
 * @create 2019-05-09 11:53
 */
@Slf4j
public class ADBTools {

    public static boolean pushFile(String deviceId, String localFile, String remoteFile) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("push");
        cmd.addArgument(localFile);
        cmd.addArgument(remoteFile);

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.setTimeout(INFINITE_TIMEOUT);
            adbexe.exec();
            return true;
        } catch (IOException e) {
            String error = adbexe.getError();
            String output = adbexe.getOutput();
            log.error("Push 文件夹 TO 设备错误, {}, output: {}", cmd.toString(), output);
        }
        return false;
    }

    public static String chmod(String deviceId, String remoteFile, String mode) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("shell");
        cmd.addArgument("chmod");
        cmd.addArgument("-R");
        cmd.addArgument(mode);
        cmd.addArgument(remoteFile);

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.exec();
        } catch (IOException e) {
            String error = adbexe.getError();
        }
        return adbexe.getOutput();
    }

    public static Boolean forward(String deviceId, int port, String socketName) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("forward");
        cmd.addArgument(String.format("tcp:%d", port));
        cmd.addArgument(String.format("localabstract:%s", socketName));

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.exec();
            return true;
        } catch (IOException e) {
            String error = adbexe.getError();
            return false;
        }
    }

    public static Boolean forwardRemove(String deviceId, int port) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("forward");
        cmd.addArgument("--remove");
        cmd.addArgument(String.format("tcp:%d", port));

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.exec();
            return true;
        } catch (IOException e) {
            String error = adbexe.getError();
        }
        return false;
    }

    public static Boolean reverse(String deviceId, String sockName, int port) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("reverse");
        cmd.addArgument(String.format("localabstract:%s", sockName));
        cmd.addArgument(String.format("tcp:%d", port));

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.exec();
            return true;
        } catch (IOException e) {
            String error = adbexe.getError();
            return false;
        }
    }

    public static Boolean reverseRemove(String deviceId, String sockName) {
        CommandLine cmd = new CommandLine(AndroidSdk.adb());
        cmd.addArgument("-s");
        cmd.addArgument(deviceId);
        cmd.addArgument("reverse");
        cmd.addArgument("--remove");
        cmd.addArgument(String.format("localabstract:%s", sockName));

        UTF8CommonExecs adbexe = new UTF8CommonExecs(cmd);
        try {
            adbexe.exec();
            return true;
        } catch (IOException e) {
            String error = adbexe.getError();
            return false;
        }
    }

    public static PhysicalSize getPhysicalSize(String deviceId) {
        String ret = command(deviceId, new String[]{"wm", "size"});

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

    public static String command(String deviceId, String[] command) {
        CommandLine commandLine = getADBCommandLine(deviceId);
        commandLine.addArgument("shell");
        commandLine.addArguments(command);
        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);
        try {
            adbexe.exec();
        } catch (IOException e) {
            log.error("adb command error\n,{}\n {}\n {}", commandLine.toString().replace(",", ""), adbexe.getOutput(), adbexe.getError());
        }
        return StringUtils.trim(adbexe.getOutput());
    }

    public static CommandLine getADBCommandLine(String deviceId) {
        CommandLine commandLine = new CommandLine(com.github.cosysoft.device.shell.AndroidSdk.adb());
        commandLine.addArgument("-s");
        commandLine.addArgument(deviceId);
        return commandLine;
    }
}
