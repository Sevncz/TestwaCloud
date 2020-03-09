package com.testwa.core.script.snippet;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScriptCodePython implements ScriptCode {

    @Override
    public String codeFor_findAndAssign(String strategy, String locator, String localVar, Boolean isArray, Map<String, String> suffixMap) throws Exception {
        if(!suffixMap.containsKey(strategy)) {
            throw new Exception("无法生成该策略代码");
        }
        String strategyContent = suffixMap.get(strategy);
        if (isArray) {
            return String.format("%s = driver.find_elements_by_%s(%s)", localVar, strategyContent, locator);
        }
        return String.format("%s = driver.find_element_by_%s(%s)", localVar, strategyContent, locator);
    }

    @Override
    public String codeFor_click(String varName) {
        return String.format("%s.click()", varName);
    }

    @Override
    public String codeFor_clear(String varName) {
        return String.format("%s.clear()", varName);
    }

    @Override
    public String codeFor_sendKeys(String varName, String text) {
        return String.format("%s.send_keys(%s)", varName, text);
    }

    @Override
    public String codeFor_back() {
        return "driver.back()";
    }

    @Override
    public String codeFor_tap(String varName, String x, String y) {
        return String.format("TouchAction(driver).tap(x=%s, y=%s).perform()", x, y);
    }

    @Override
    public String codeFor_swipe(String varName, String x1, String y1, String x2, String y2) {
        return String.format("TouchAction(driver) \n" +
                "  .press(x=%s, y=%s) \n" +
                "  .move_to(x=%s, y=%s) \n" +
                "  .release() \\\n" +
                "  .perform()", x1, y1, x2, y2);
    }

    @Override
    public String codeFor_getCurrentActivity() {
        return "activity_name = driver.current_activity";
    }

    @Override
    public String codeFor_getCurrentPackage() {
        return "package_name = driver.current_package";
    }

    @Override
    public String codeFor_installAppOnDevice(String varName, String app) {
        return String.format("driver.install_app('%s')", app);
    }

    @Override
    public String codeFor_isAppInstalledOnDevice(String varName, String app) {
        return String.format("is_app_installed = driver.isAppInstalled(\"%s\")", app);
    }

    @Override
    public String codeFor_launchApp() {
        return "driver.launch_app()";
    }

    @Override
    public String codeFor_backgroundApp(String varName, String timeout) {
        return String.format("driver.background_app(%s)", timeout);
    }

    @Override
    public String codeFor_closeApp() {
        return "driver.close_app()";
    }

    @Override
    public String codeFor_resetApp() {
        return "driver.reset()";
    }

    @Override
    public String codeFor_removeAppFromDevice(String varName, String app) {
        return null;
    }

    @Override
    public String codeFor_getAppStrings(String varName, String language, String file) {
        return null;
    }

    @Override
    public String codeFor_getClipboard() {
        return null;
    }

    @Override
    public String codeFor_setClipboard(String varName, String clipboardText) {
        return null;
    }

    @Override
    public String codeFor_pressKeycode(String varName, String keyCode, String metaState, String flags) {
        return null;
    }

    @Override
    public String codeFor_longPressKeycode(String varName, String keyCode, String metaState, String flags) {
        return null;
    }

    @Override
    public String codeFor_hideDeviceKeyboard() {
        return null;
    }

    @Override
    public String codeFor_isKeyboardShown() {
        return null;
    }

    @Override
    public String codeFor_pushFileToDevice(String varName, String pathToInstallTo, String fileContentString) {
        return null;
    }

    @Override
    public String codeFor_pullFile(String varName, String pathToPullFrom) {
        return null;
    }

    @Override
    public String codeFor_pullFolder(String varName, String folderToPullFrom) {
        return null;
    }

    @Override
    public String codeFor_toggleAirplaneMode() {
        return null;
    }

    @Override
    public String codeFor_toggleData() {
        return null;
    }

    @Override
    public String codeFor_toggleWiFi() {
        return null;
    }

    @Override
    public String codeFor_toggleLocationServices() {
        return null;
    }

    @Override
    public String codeFor_sendSMS() {
        return null;
    }

    @Override
    public String codeFor_gsmCall() {
        return null;
    }

    @Override
    public String codeFor_gsmSignal() {
        return null;
    }

    @Override
    public String codeFor_gsmVoice() {
        return null;
    }

    @Override
    public String codeFor_shake() {
        return null;
    }

    @Override
    public String codeFor_lock(String varName, String seconds) {
        return null;
    }

    @Override
    public String codeFor_unlock() {
        return null;
    }

    @Override
    public String codeFor_isLocked() {
        return null;
    }

    @Override
    public String codeFor_rotateDevice() {
        return null;
    }

    @Override
    public String codeFor_getPerformanceData() {
        return null;
    }

    @Override
    public String codeFor_getSupportedPerformanceDataTypes() {
        return null;
    }

    @Override
    public String codeFor_performTouchId(String varName, String match) {
        return null;
    }

    @Override
    public String codeFor_toggleTouchIdEnrollment(String varName, String enroll) {
        return null;
    }

    @Override
    public String codeFor_openNotifications() {
        return null;
    }

    @Override
    public String codeFor_getDeviceTime() {
        return null;
    }

    @Override
    public String codeFor_fingerprint(String varName, String fingerprintId) {
        return null;
    }

    @Override
    public String codeFor_sessionCapabilities() {
        return null;
    }

    @Override
    public String codeFor_setPageLoadTimeout(String varName, String ms) {
        return null;
    }

    @Override
    public String codeFor_setAsyncScriptTimeout(String varName, String ms) {
        return null;
    }

    @Override
    public String codeFor_setImplicitWaitTimeout(String varName, String ms) {
        return null;
    }

    @Override
    public String codeFor_getOrientation() {
        return null;
    }

    @Override
    public String codeFor_setOrientation(String varName, String orientation) {
        return null;
    }

    @Override
    public String codeFor_getGeoLocation() {
        return null;
    }

    @Override
    public String codeFor_setGeoLocation(String varName, String latitude, String longitude, String altitude) {
        return String.format("driver.set_location(%s, %s, %s)", latitude, longitude, altitude);
    }

    @Override
    public String codeFor_logTypes() {
        return "log_types = driver.log_types()";
    }

    @Override
    public String codeFor_log(String varName, String logType) {
        return String.format("logs = driver.get_log('%s')", logType);
    }

    @Override
    public String codeFor_updateSettings(String varName, String settingsJson) {
        return String.format("driver.update_settings(%s))", settingsJson);
    }

    @Override
    public String codeFor_settings() {
        return "settings = driver.get_settings";
    }
}
