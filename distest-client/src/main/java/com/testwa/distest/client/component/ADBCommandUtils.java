package com.testwa.distest.client.component;import com.github.cosysoft.device.android.AndroidApp;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.github.cosysoft.device.shell.AndroidSdk;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.core.utils.TimeUtil;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;/** * @Program: distest * @Description: 把adb的一些常用操作集合起来 * @Author: wen * @Create: 2018-05-17 20:54 **/@Slf4jpublic class ADBCommandUtils {    private static CommandLine getADBCommandLine(String deviceId) {        CommandLine commandLine = new CommandLine(AndroidSdk.adb());        commandLine.addArgument("-s");        commandLine.addArgument(deviceId);        return commandLine;    }    public static void installApp(String deviceId, String appLocalPath, Long timeout){        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("install");        commandLine.addArgument("-r");        commandLine.addArgument(appLocalPath);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            // 设置超时            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("install apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void launcherApp(String deviceId, String appLocalPath, Long timeout) {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        String mainActivity = androidApp.getMainActivity().replace(androidApp.getBasePackage(), "");        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("start");        commandLine.addArgument("-a");        commandLine.addArgument("android.intent.action.MAIN");        commandLine.addArgument("-n");        commandLine.addArgument(androidApp.getBasePackage() + "/" + mainActivity);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            // 设置超时            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("launch apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void uninstallApp(String deviceId, String basePackage, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("uninstall");        commandLine.addArgument(basePackage);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("uninstall apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void uninstallAppPath(String deviceId, String appLocalPath, Long timeout) {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("uninstall");        commandLine.addArgument(androidApp.getBasePackage());        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("uninstall apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputTextADBKeyBoard(String deviceId, String text, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("broadcast");        commandLine.addArgument("-a");        commandLine.addArgument("ADB_INPUT_TEXT");        commandLine.addArgument("--es");        commandLine.addArgument("msg");        commandLine.addArgument(text);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input text ADBKeyBoard return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputTextOriginal(String deviceId, String text, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("input");        commandLine.addArgument("text");        commandLine.addArgument(text);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input text original return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputCode(String deviceId, int code, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("input");        commandLine.addArgument("keyevent");        commandLine.addArgument(String.valueOf(code));        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input code return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputCodeADBKeyBoard(String deviceId, int code, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("broadcast");        commandLine.addArgument("-a");        commandLine.addArgument("ADB_INPUT_CODE");        commandLine.addArgument("--ei");        commandLine.addArgument("code");        commandLine.addArgument(String.valueOf(code));        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input code ADBKeyBoard return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void switchADBKeyBoard(String deviceId) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("ime");        commandLine.addArgument("set");        commandLine.addArgument("com.android.adbkeyboard/.AdbIME");        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("switch to ADBKeyBoard  {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void switchOriginalKeyboard(String deviceId) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("ime");        commandLine.addArgument("set");        commandLine.addArgument("com.nuance.swype.dtc/com.nuance.swype.input.IME");        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("switch back to original virtual keyboard {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void openWeb(String deviceId, String url) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("start");        commandLine.addArgument("-a");        commandLine.addArgument("android.intent.action.VIEW");        commandLine.addArgument("-d");        commandLine.addArgument(url);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("open web return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void shell(String deviceId, String shellcmd) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument(shellcmd);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(5000L);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("shell return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static boolean isInstalledApp(String deviceId, String appLocalPath) {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("pm");        commandLine.addArgument("list");        commandLine.addArgument("packages");        commandLine.addArgument(androidApp.getBasePackage());        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("is install App return {}, time {}ms", output.trim(), end-start);            if(StringUtils.isNotBlank(output) && output.contains(androidApp.getBasePackage())){                return true;            }        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }        return false;    }    public static boolean isInstalledBasepackage(String deviceId, String basepackage) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("pm");        commandLine.addArgument("list");        commandLine.addArgument("packages");        commandLine.addArgument(basepackage);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("is install App return {}, time {}ms", output.trim(), end-start);            if(StringUtils.isNotBlank(output) && output.contains(basepackage)){                return true;            }        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }        return false;    }}