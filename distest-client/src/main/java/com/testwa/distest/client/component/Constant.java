package com.testwa.distest.client.component;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Constant {
    private static final String USER_HOME = System.getProperty("user.home");

    public static final String KEYBOARD_SERVICE_APK = "keyboardservice-debug.apk";

    public static final String MAXIM_BIN = "maxim";

    public static final String AGENT_TMP_DIR = Paths.get(USER_HOME, "testwa_agent").toString();

	public static final String localAppPath = Paths.get(USER_HOME, "testwa_agent", "app").toString();
	public static final String localScriptPath = Paths.get(USER_HOME, "testwa_agent", "script").toString();
	public static final String localScriptTmpPath = Paths.get(USER_HOME, "testwa_agent", "script_tmp").toString();

	public static final String localAppiumLogPath = Paths.get(USER_HOME, "testwa_agent", "appium").toString();
	public static final String localScreenshotPath = Paths.get(USER_HOME, "testwa_agent", "capture").toString();
	public static final String localLogcatPath = Paths.get(USER_HOME, "testwa_agent", "logcat").toString();

	public static final String localActionScreenPath = Paths.get(USER_HOME, "testwa_agent", "action").toString();

    public static final String localVideoPath = Paths.get(USER_HOME, "testwa_agent", "video").toString();
    public static final String localCrawlerOutPath = Paths.get(USER_HOME, "testwa_agent", "crawler").toString();

    // ios-deploy
    public static final String IOS_DEPLOY = "ios-deploy";
    public static final String IOS_DEPLOY_BIN = "ios-deploy";
    public static final String IOS_SYSLOG = "idevicesyslog";

    public static final String APPIUM_LIB = "appium";
    public static final String APPIUM_UI2 = "appium-uiautomator2-server.apk";
    public static final String APPIUM_UI2_DEBUG = "appium-uiautomator2-server-debug-androidTest.apk";

    public static final String XCODEBUILD_CONFIG_DIR = Paths.get(USER_HOME, "testwa_agent", "xcode").toString();


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