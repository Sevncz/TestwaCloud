package com.testwa.core;

/**
 * Created by wen on 03/06/2017.
 */
public class WebsocketEvent {

    public static final String ON_TESTCASE_RUN = "testcaseRun";
    public static final String ON_SCREEN_SHOW_START = "screenStart";
    public static final String ON_SCREEN_SHOW_STOP = "screenStop";
    public static final String ON_APP_INSTALL = "installApp";
    public static final String ON_APP_UNINSTALL = "uninstallApp";
    public static final String ON_LOGCAT_SHOW_START = "logcatStart";
    public static final String ON_LOGCAT_SHOW_STOP = "logcatStop";
    public static final String ON_REGISTER = "agentRegister";
    public static final String ON_DEVICE_DISCONNECT = "deviceDisconnect";
    public static final String ON_DEVICE_OFFLINE = "deviceOffLine";

    public static final String FB_RUNNGING_LOG = "feedback.runninglog";
    public static final String FB_APPIUM_ERROR = "feedback.appium.error";
    public static final String FB_REPORT_SDETAIL = "feedback.report.sdetail";
//    public static final String FB_DEVICE = "feedback.device";
    public static final String FB_RUNNING_SCREEN = "feedback.running.screen";
    public static final String FB_SCRIPT_END = "feedback.script.end";

    public static final String CONNECT_SESSION = "connect.session";
    public static final String DEVICE = "device";



}
