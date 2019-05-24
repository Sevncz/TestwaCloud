package com.testwa.distest.client.android;


import com.github.cosysoft.device.shell.OS;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;

public class AndroidSdk {
    public static final String ANDROID_FOLDER_PREFIX = "android-";
    public static final String ANDROID_HOME = "ANDROID_HOME";

    public AndroidSdk() {
    }

    public static File adb() {
        return new File(platformToolsHome(), "adb" + OS.platformExecutableSuffixExe());
    }

    public static File aapt() {
        StringBuffer command = new StringBuffer();
        command.append("aapt");
        command.append(OS.platformExecutableSuffixExe());
        File platformToolsAapt = new File(platformToolsHome(), command.toString());
        if (platformToolsAapt.isFile()) {
            return platformToolsAapt;
        } else {
            File buildToolsFolder = buildToolsHome();
            return new File(findLatestAndroidPlatformFolder(buildToolsFolder, "Command 'aapt' was not found inside the Android SDK. Please update to the latest development tools and try again."), command.toString());
        }
    }

    public static File android() {
        StringBuffer command = new StringBuffer();
        command.append(toolsHome());
        return new File(toolsHome(), "android" + OS.platformExecutableSuffixBat());
    }

    public static File emulator() {
        return new File(toolsHome(), "emulator" + OS.platformExecutableSuffixExe());
    }

    private static File toolsHome() {
        StringBuffer command = new StringBuffer();
        command.append(androidHome());
        command.append(File.separator);
        command.append("tools");
        command.append(File.separator);
        return new File(command.toString());
    }

    private static File buildToolsHome() {
        StringBuffer command = new StringBuffer();
        command.append(androidHome());
        command.append(File.separator);
        command.append("build-tools");
        command.append(File.separator);
        return new File(command.toString());
    }

    private static File platformToolsHome() {
        StringBuffer command = new StringBuffer();
        command.append(androidHome());
        command.append(File.separator);
        command.append("platform-tools");
        command.append(File.separator);
        return new File(command.toString());
    }

    public static String androidHome() {
        String androidHome = System.getenv("ANDROID_HOME");
        if (androidHome == null) {
            throw new RuntimeException("Environment variable 'ANDROID_HOME' was not found!");
        } else {
            return androidHome;
        }
    }

    public static String androidJar() {
        String platformsRootFolder = androidHome() + File.separator + "platforms";
        File platformsFolder = new File(platformsRootFolder);
        return (new File(findLatestAndroidPlatformFolder(platformsFolder, "No installed Android APIs have been found."), "android.jar")).getAbsolutePath();
    }

    protected static File findLatestAndroidPlatformFolder(File rootFolder, String errorMessage) {
        File[] androidApis = rootFolder.listFiles(new AndroidSdk.AndroidFileFilter());
        if (androidApis != null && androidApis.length != 0) {
            Arrays.sort(androidApis, Collections.reverseOrder());
            return androidApis[0].getAbsoluteFile();
        } else {
            throw new RuntimeException(errorMessage);
        }
    }

    public static class AndroidFileFilter implements FileFilter {
        public AndroidFileFilter() {
        }

        @Override
        public boolean accept(File pathname) {
            String fileName = pathname.getName();
            String regex = "\\d{2}\\.\\d{1}\\.\\d{1}";
            return fileName.matches(regex) || fileName.startsWith("android-");
        }
    }
}
