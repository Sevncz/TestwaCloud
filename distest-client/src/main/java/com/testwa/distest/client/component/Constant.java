package com.testwa.distest.client.component;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Constant {

    public static final String KEYBOARD_SERVICE_APK = "keyboardservice-debug.apk";

    public static final String MAXIM_BIN = "maxim";

    public static final String AGENT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent").toString();

	public static final String localAppPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "app").toString();
	public static final String localScriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "script").toString();
	public static final String localScriptTmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "script_tmp").toString();

	public static final String localAppiumLogPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "appium").toString();
	public static final String localScreenshotPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "screenshot").toString();
	public static final String localLogcatPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "logcat").toString();

	public static final String localActionScreenPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "action").toString();

    public static final String localVideoPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "video").toString();
    public static final String localCrawlerOutPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "crawler").toString();

    // ios-deploy
    public static final String IOS_DEPLOY = "ios-deploy";
    public static final String IOS_DEPLOY_BIN = "ios-deploy";
    public static final String IOS_SYSLOG = "idevicesyslog";

    public static final String APPIUM_LIB = "appium";
    public static final String APPIUM_UI2 = "appium-uiautomator2-server.apk";
    public static final String APPIUM_UI2_DEBUG = "appium-uiautomator2-server-debug-androidTest.apk";


    public static String getKeyboardService() {
        return "keyboardservice" + File.separator + KEYBOARD_SERVICE_APK;
    }

    public static String getIOSDeploy() {
        return IOS_DEPLOY + File.separator + IOS_DEPLOY_BIN;
    }

    public static String getAppiumUI2() {
        return APPIUM_LIB + File.separator + APPIUM_UI2;
    }
    public static String getAppiumUI2Debug() {
        return APPIUM_LIB + File.separator + APPIUM_UI2_DEBUG;
    }

}