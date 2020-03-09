package com.testwa.core.script.snippet;

import java.util.Map;

public interface ScriptCode {

    String codeFor_findAndAssign(String strategy, String locator, String localVar, Boolean isArray, Map<String, String> suffixMap) throws Exception;

    String codeFor_click(String varName);

    String codeFor_clear(String varName);

    String codeFor_sendKeys(String varName, String text);

    String codeFor_back();

    String codeFor_tap(String varName, String x, String y);

    String codeFor_swipe(String varName, String x1, String y1, String x2, String y2);

    String codeFor_getCurrentActivity();

    String codeFor_getCurrentPackage();

    String codeFor_installAppOnDevice(String varName, String app);

    String codeFor_isAppInstalledOnDevice(String varName, String app);

    String codeFor_launchApp();

    String codeFor_backgroundApp(String varName, String timeout);

    String codeFor_closeApp();

    String codeFor_resetApp();

    String codeFor_removeAppFromDevice(String varName, String app);

    String codeFor_getAppStrings(String varName, String language, String file);

    String codeFor_getClipboard();

    String codeFor_setClipboard(String varName, String clipboardText);

    String codeFor_pressKeycode(String varName, String keyCode, String metaState, String flags);

    String codeFor_longPressKeycode(String varName, String keyCode, String metaState, String flags);

    String codeFor_hideDeviceKeyboard();

    String codeFor_isKeyboardShown();

    String codeFor_pushFileToDevice(String varName, String pathToInstallTo, String fileContentString);

    String codeFor_pullFile(String varName, String pathToPullFrom);

    String codeFor_pullFolder(String varName, String folderToPullFrom);

    String codeFor_toggleAirplaneMode();

    String codeFor_toggleData();

    String codeFor_toggleWiFi();

    String codeFor_toggleLocationServices();

    String codeFor_sendSMS();

    String codeFor_gsmCall();

    String codeFor_gsmSignal();

    String codeFor_gsmVoice();

    String codeFor_shake();

    String codeFor_lock(String varName, String seconds);

    String codeFor_unlock();

    String codeFor_isLocked();

    String codeFor_rotateDevice();

    String codeFor_getPerformanceData();

    String codeFor_getSupportedPerformanceDataTypes();

    String codeFor_performTouchId(String varName, String match);

    String codeFor_toggleTouchIdEnrollment(String varName, String enroll);

    String codeFor_openNotifications();

    String codeFor_getDeviceTime();

    String codeFor_fingerprint(String varName, String fingerprintId);

    String codeFor_sessionCapabilities();

    String codeFor_setPageLoadTimeout(String varName, String ms);

    String codeFor_setAsyncScriptTimeout(String varName, String ms);

    String codeFor_setImplicitWaitTimeout(String varName, String ms);

    String codeFor_getOrientation();

    String codeFor_setOrientation(String varName, String orientation);

    String codeFor_getGeoLocation();

    String codeFor_setGeoLocation(String varName, String latitude, String longitude, String altitude);

    String codeFor_logTypes();

    String codeFor_log(String varName, String logType);

    String codeFor_updateSettings(String varName, String settingsJson);

    String codeFor_settings();


}
