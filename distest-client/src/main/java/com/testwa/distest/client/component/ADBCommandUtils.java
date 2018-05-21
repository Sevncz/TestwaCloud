package com.testwa.distest.client.component;import com.github.cosysoft.device.android.AndroidApp;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.github.cosysoft.device.shell.AndroidSdk;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.exception.InstallAppException;import com.testwa.distest.client.exception.LaunchAppException;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;/** * @Program: distest * @Description: 把adb的一些常用操作集合起来 * @Author: wen * @Create: 2018-05-17 20:54 **/@Slf4jpublic class ADBCommandUtils {    private static final String SDCARD_DIR = "/sdcard";    private static final String MAX_SCREEN = "max-screen";    private static final String CRASH_LOG = "/sdcard/crash-dump.log";    private static CommandLine getADBCommandLine(String deviceId) {        CommandLine commandLine = new CommandLine(AndroidSdk.adb());        commandLine.addArgument("-s");        commandLine.addArgument(deviceId);        return commandLine;    }    public static void installApp(String deviceId, String appLocalPath, Long timeout) throws InstallAppException {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("install");        commandLine.addArgument("-r");        commandLine.addArgument(appLocalPath);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            // 设置超时            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("install apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());            throw new InstallAppException(error);        }    }    public static StepResult launcherApp(String deviceId, String appLocalPath) throws LaunchAppException {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        String mainActivity = androidApp.getMainActivity().replace(androidApp.getBasePackage(), "");        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("start");        commandLine.addArgument("-W");        commandLine.addArgument("-n");        commandLine.addArgument(androidApp.getBasePackage() + "/" + mainActivity);        StepResult stepResult = new StepResult();        stepResult.setAction("启动应用");        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.exec();            String output = adbexe.getOutput();            /*            Starting: Intent { act=android.intent.action.MAIN cmp=so.ofo.labofo/.activities.EntryActivity }            Status: ok            Activity: com.lbe.security.miui/com.android.packageinstaller.permission.ui.GrantPermissionsActivity            ThisTime: 697            TotalTime: 4019            WaitTime: 4051            Complete, time 12390ms             */            String[] result = output.split("\n");            for(String l : result){                if(l.startsWith("Status")){                    String[] lineSplit = l.split(":");                    String status = lineSplit[1].trim();                    if("ok".equals(status)){                        stepResult.setStatus(true);                    }else{                        stepResult.setStatus(false);                    }                }                if(l.startsWith("TotalTime")){                    String[] lineSplit = l.split(":");                    String totalTime = lineSplit[1].trim();                    stepResult.setTotalTime(Long.parseLong(totalTime));                }                if(l.startsWith("Error")){                    throw new LaunchAppException(output);                }            }            log.info("launch apk return {}, result {}", output.trim(), stepResult.toString());        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());            throw new LaunchAppException(error);        }        return stepResult;    }    public static void uninstallApp(String deviceId, String basePackage) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("uninstall");        commandLine.addArgument(basePackage);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("uninstall apk return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void uninstallAppPath(String deviceId, String appLocalPath) {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        uninstallApp(deviceId, androidApp.getBasePackage());    }    public static void inputTextADBKeyBoard(String deviceId, String text, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("broadcast");        commandLine.addArgument("-a");        commandLine.addArgument("ADB_INPUT_TEXT");        commandLine.addArgument("--es");        commandLine.addArgument("msg");        commandLine.addArgument(text);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input text ADBKeyBoard return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputTextOriginal(String deviceId, String text, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("input");        commandLine.addArgument("text");        commandLine.addArgument(text);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input text original return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputCode(String deviceId, int code, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("input");        commandLine.addArgument("keyevent");        commandLine.addArgument(String.valueOf(code));        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input code return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void inputCodeADBKeyBoard(String deviceId, int code, Long timeout) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("broadcast");        commandLine.addArgument("-a");        commandLine.addArgument("ADB_INPUT_CODE");        commandLine.addArgument("--ei");        commandLine.addArgument("code");        commandLine.addArgument(String.valueOf(code));        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(timeout);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("input code ADBKeyBoard return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void switchADBKeyBoard(String deviceId) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("ime");        commandLine.addArgument("set");        commandLine.addArgument("com.android.adbkeyboard/.AdbIME");        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("switch to ADBKeyBoard  {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void switchOriginalKeyboard(String deviceId) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("ime");        commandLine.addArgument("set");        commandLine.addArgument("com.nuance.swype.dtc/com.nuance.swype.input.IME");        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("switch back to original virtual keyboard {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void openWeb(String deviceId, String url) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("am");        commandLine.addArgument("start");        commandLine.addArgument("-a");        commandLine.addArgument("android.intent.action.VIEW");        commandLine.addArgument("-d");        commandLine.addArgument(url);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("open web return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static void shell(String deviceId, String shellcmd) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument(shellcmd);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            adbexe.setTimeout(5000L);            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("shell return {}, time {}ms", output.trim(), end-start);        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }    }    public static boolean isInstalledApp(String deviceId, String appLocalPath) {        AndroidApp androidApp = new DefaultAndroidApp(new File(appLocalPath));        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("pm");        commandLine.addArgument("list");        commandLine.addArgument("packages");        commandLine.addArgument(androidApp.getBasePackage());        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("is install App return {}, time {}ms", output.trim(), end-start);            if(StringUtils.isNotBlank(output) && output.contains(androidApp.getBasePackage())){                return true;            }        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }        return false;    }    public static boolean isInstalledBasepackage(String deviceId, String basepackage) {        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("pm");        commandLine.addArgument("list");        commandLine.addArgument("packages");        commandLine.addArgument(basepackage);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        try {            Long start = TimeUtil.getTimestampLong();            adbexe.exec();            String output = adbexe.getOutput();            Long end = TimeUtil.getTimestampLong();            log.info("is install App return {}, time {}ms", output.trim(), end-start);            if(StringUtils.isNotBlank(output) && output.contains(basepackage)){                return true;            }        } catch (IOException e) {            String error = adbexe.getError();            log.error("ADB 命令执行失败 cmd: {}  error: {}  exception: {}", commandLine.toString(), error, e.getMessage());        }        return false;    }    /**     *@Description: 1、创建文件夹；     *              2、push Maxim.jar；     *              3、run；     *              4、pull截图     *              adb shell CLASSPATH=/sdcard/monkey.jar:/sdcard/framework.jar exec app_process /system/bin tv.panda.test.monkey.Monkey -p so.ofo.labofo --uiautomatormix --running-minutes 1 -v -v --throttle 2000 --output-directory /sdcard/max-screen/     *@Param: [deviceId, appid, runningTime]     *@Return: java.lang.String     *@Author: wen     *@Date: 2018/5/21     */    public static String monkey(String deviceId, String appid, int runningTime) {        String resourcesPath = Config.getString("distest.agent.resources");        Path localPullDir = Paths.get(Constant.AGENT_TMP_DIR, deviceId, TimeUtil.getTimestampForFile());        Path maximDir = Paths.get(resourcesPath, Constant.MAXIM_BIN);        try {            Files.list(maximDir).forEach( p -> {                pushFile(deviceId, p.toString(), SDCARD_DIR);            });        } catch (IOException e) {            e.printStackTrace();        }        String outputdir = SDCARD_DIR + "/" + MAX_SCREEN + "/";        removeDir(deviceId, outputdir);        CommandLine commandLine = getADBCommandLine(deviceId);        commandLine.addArgument("shell");        commandLine.addArgument("CLASSPATH=/sdcard/monkey.jar:/sdcard/framework.jar");        commandLine.addArgument("exec");        commandLine.addArgument("app_process");        commandLine.addArgument("/system/bin");        commandLine.addArgument("tv.panda.test.monkey.Monkey");        commandLine.addArgument("-p");        commandLine.addArgument(appid);        commandLine.addArgument("--uiautomatormix");        commandLine.addArgument("--running-minutes");        commandLine.addArgument(String.valueOf(runningTime));//        commandLine.addArgument("-v");//        commandLine.addArgument("-v");        commandLine.addArgument("--throttle");        commandLine.addArgument("2000");        commandLine.addArgument("--output-directory");        commandLine.addArgument(outputdir);        UTF8CommonExecs adbexe = new UTF8CommonExecs(commandLine);        Long start = TimeUtil.getTimestampLong();        try {            adbexe.setTimeout((runningTime + 1) * 60 * 1000L);            adbexe.exec();        } catch (IOException e) {            Long end = TimeUtil.getTimestampLong();            String output = adbexe.getOutput();            log.info("monkey running time {}ms", end-start);            if((end - start) < runningTime * 60 * 1000){                log.error("出异常了, {}", output);            }        } finally {            pullDir(deviceId, outputdir, localPullDir.toString());        }        return localPullDir.toString() + File.separator + MAX_SCREEN;    }    public static void pushFile(String deviceId, String localFile, String remoteDir) {        CommandLine pushCmd = getADBCommandLine(deviceId);        pushCmd.addArgument("push");        pushCmd.addArgument(localFile);        pushCmd.addArgument(remoteDir);        UTF8CommonExecs adbexe = new UTF8CommonExecs(pushCmd);        try {            adbexe.exec();        } catch (IOException e) {            String error = adbexe.getError();            log.error(error);        }    }    public static void pullDir(String deviceId, String remoteDir, String localDir) {        try {            Files.createDirectories(Paths.get(localDir));        } catch (IOException e) {            log.error("创建临时文件失败, {}", localDir, e);        }        CommandLine pushCmd = getADBCommandLine(deviceId);        pushCmd.addArgument("pull");        pushCmd.addArgument(remoteDir);        pushCmd.addArgument(localDir);        UTF8CommonExecs adbexe = new UTF8CommonExecs(pushCmd);        try {            adbexe.exec();        } catch (IOException e) {            String error = adbexe.getError();            log.error(error);        }    }    public static void createDir(String deviceId, String remoteDir) {        CommandLine pushCmd = getADBCommandLine(deviceId);        pushCmd.addArgument("shell");        pushCmd.addArgument("mkdir");        pushCmd.addArgument(remoteDir);        UTF8CommonExecs adbexe = new UTF8CommonExecs(pushCmd);        try {            adbexe.exec();        } catch (IOException e) {            String error = adbexe.getError();            log.error(error);        }    }    public static void removeDir(String deviceId, String remoteDir) {        CommandLine pushCmd = getADBCommandLine(deviceId);        pushCmd.addArgument("shell");        pushCmd.addArgument("rm");        pushCmd.addArgument("-r");        pushCmd.addArgument(remoteDir);        UTF8CommonExecs adbexe = new UTF8CommonExecs(pushCmd);        try {            adbexe.exec();        } catch (IOException e) {            String error = adbexe.getError();            log.error(error);        }    }}