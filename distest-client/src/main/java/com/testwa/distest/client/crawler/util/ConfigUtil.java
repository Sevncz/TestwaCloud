package com.testwa.distest.client.crawler.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Ma Yi on 2017/4/27.
 */
public class ConfigUtil {
    public static Logger log = LoggerFactory.getLogger(ConfigUtil.class);
    private static String udid;
    private static String port;
    private static ConfigUtil configUtil;
    private static Map<String,Object> configItems;
    private static  String rootDir;
    private static boolean showDomXML = false;

    private static boolean dbLogEnabled = false;
    private static boolean perLogEnabled = false;




    private static boolean videoVertial = true;

    public static boolean isShowDomXML() {
        return showDomXML;
    }

    //Android
    public static final String ANDROID_BOTTOM_TAB_BAR_ID = "ANDROID_BOTTOM_TAB_BAR_ID";
    public static final String ANDROID_EXCLUDE_TYPE = "ANDROID_EXCLUDE_TYPE";
    public static final String ANDROID_CLICK_XPATH_HEADER = "ANDROID_CLICK_XPATH_HEADER";
    public static final String IOS_EXCLUDE_BAR = "IOS_EXCLUDE_BAR";
    public static final String ANDROID_USERNAME = "ANDROID_USERNAME";
    public static final String ANDROID_PASSWORD = "ANDROID_PASSWORD";
    public static final String ANDROID_LOGIN_BUTTON = "ANDROID_LOGIN_BUTTON";
    public static final String ANDROID_BACK_KEY = "ANDROID_BACK_KEY";

    //iOS
    public static final String IOS_EXCLUDE_TYPE = "IOS_EXCLUDE_TYPE";
    public static final String IOS_BOTTOM_TAB_BAR_TYPE = "IOS_BOTTOM_TAB_BAR_TYPE";
    public static final String IOS_CLICK_XPATH_HEADER = "IOS_CLICK_XPATH_HEADER";
    public static final String IOS_USERNAME = "IOS_USERNAME";
    public static final String IOS_PASSWORD = "IOS_PASSWORD";
    public static final String IOS_LOGIN_BUTTON = "IOS_LOGIN_BUTTON";
    public static final String IOS_IPA_NAME = "IOS_IPA_NAME";
    public static final String IOS_WDA_PORT = "IOS_WDA_PORT";
    public static final String IOS_BUNDLE_ID = "IOS_BUNDLE_ID";
    public static final String IOS_BACK_KEY = "IOS_BACK_KEY";
    public static final String IOS_BUNDLE_NAME = "IOS_BUNDLE_NAME";

    //GENERAL CONFIG ITEM
    public static final String MAX_DEPTH = "MAX_DEPTH";
    public static final String CRASH_PIC_COUNT = "CRASH_PIC_COUNT";
    public static final String SCREEN_SHOT = "screenshot";
    public static final String IGNORE_CRASH = "IGNORE_CRASH";
    public static final String ENABLE_VERTICAL_SWIPE = "ENABLE_VERTICAL_SWIPE";
    public static final String DOM_DISPLAY = "DOM_DISPLAY" ;
    public static final String REMOVE_BOTTOM_BOUND = "REMOVE_BOTTOM_BOUND";
    public static final String VIDEO_VERTICAL = "VIDEO_VERTICAL";

    //INFLUXDB
    public static final String DB_PORT = "DB_PORT";
    public static final String DB_IP = "DB_IP";

    //DEFAULT VALUE ITEM
    public static final String CRAWLER_RUNNING_TIME = "CRAWLER_RUNNING_TIME";

    //LIST
    public static final String ANDROID_VALID_PACKAGE_LIST = "ANDROID_VALID_PACKAGE_LIST";
    public static final String IOS_VALID_BUNDLE_LIST = "IOS_VALID_BUNDLE_LIST";
    public static final String ITEM_BLACKLIST = "ITEM_BLACKLIST";
    public static final String ITEM_WHITE_LIST = "ITEM_WHITE_LIST";
    public static final String NODE_NAME_EXCLUDE_LIST = "NODE_NAME_EXCLUDE_LIST";
    public static final String STRUCTURE_NODE_NAME_EXCLUDE_LIST = "STRUCTURE_NODE_NAME_EXCLUDE_LIST";
    public static final String PRESS_BACK_KEY_PACKAGE_LIST = "PRESS_BACK_KEY_PACKAGE_LIST";
    public static final String XPATH = "XPATH";
    public static final String ACTION = "ACTION";
    public static final String VALUE = "VALUE";
    public static final String INPUT_CLASS_LIST = "INPUT_CLASS_LIST";
    public static final String INPUT_TEXT_LIST = "INPUT_TEXT_LIST";
    public static final String BACK_KEY_TRIGGER_LIST = "BACK_KEY_TRIGGER_LIST";

    //Monkey
    public static final String SWIPE_RATIO = "SWIPE_RATIO";
    public static final String CLICK_RATIO = "CLICK_RATIO";
    public static final String CLICK_SPECIAL_POINT_RATIO = "CLICK_SPECIAL_POINT_RATIO";
    public static final String RESTART_APP_RATIO = "RESTART_APP_RATIO";
    public static final String HOME_KEY_RATIO = "HOME_KEY_RATIO";
    public static final String LONG_PRESS_RATIO = "LONG_PRESS_RATIO";
    public static final String DOUBLE_TAP_RATIO = "DOUBLE_TAP_RATIO";
    public static final String PINCH_RATIO = "PINCH_RATIO";
    public static final String UNPINCH_RATIO = "UNPINCH_RATIO";
    public static final String DRAG_RATIO = "DRAG_RATIO";
    public static final String BACK_KEY_RATIO = "BACK_KEY_RATIO";

    //LOG
    public static final String DB_LOG = "DB_LOG";
    public static final String PERF_LOG = "PERF_LOG";

    public static final String MONKEY_SPECIAL_POINT_LIST = "MONKEY_SPECIAL_POINT_LIST";
    public static final String LONG_PRESS_LIST = "LONG_PRESS_LIST";
    public static final ArrayList<String> MONKEY_EVENT_RATION_LIST = new ArrayList<>(Arrays.asList(RESTART_APP_RATIO,CLICK_RATIO,SWIPE_RATIO,
            LONG_PRESS_RATIO,CLICK_SPECIAL_POINT_RATIO,HOME_KEY_RATIO,
            DOUBLE_TAP_RATIO,PINCH_RATIO,UNPINCH_RATIO,DRAG_RATIO,BACK_KEY_RATIO));
    public static final String MONKEY_RUNNING_TIME = "MONKEY_RUNNING_TIME";
    private static long clickCount;

    //MINI Programme
    public static final String MINI_PROGRAM_NAME = "MINI_PROGRAM_NAME";
    public static final String MINI_PROGRAM_PROCESS = "MINI_PROGRAM_PROCESS";


    public static ConfigUtil initialize(String file, String udid){
        log.info("Method: initialize");

        rootDir = System.getProperty("user.dir") + File.separator + udid + "-" + Util.getCurrentTimeFormat();
//
//        if(null != configUtil){
//            return configUtil;
//        }

        try {
            log.info("Reading config file " + file);

            setUdid(udid);

            InputStream input = new FileInputStream(new File(file));
            Yaml yaml = new Yaml();
            configUtil = new ConfigUtil();

            Map<String, Object> map = yaml.load(input);

            configItems = new HashMap<>();
            //初始化的顺序很重要
            //1.先设通用的值 GENERAL  2.设默认值 DEFAULT_VALUE 3.根据serial值去覆盖默认的属性值 4.然后其它值
            List<String> keyList = new ArrayList(Arrays.asList("GENERAL","DEFAULT_VALUE","MONKEY","LIST","CRITICAL_ELEMENT","MONKEY_LIST","MINI_PROGRAM","LOG"));
            if(map.get(udid)!=null){
                keyList.add(udid);
            }

            for(String key : keyList){
                Map<String,Object> tempMap = (Map<String,Object>)map.get(key);
                if(tempMap != null){
                    for(String itemKey : tempMap.keySet()){
                        configItems.put(itemKey,tempMap.get(itemKey));
                    }
                }
            }

            port = getStringValue("PORT");
            clickCount = getLongValue("MAX_CLICK_COUNT");
            dbLogEnabled = ConfigUtil.getBooleanValue(DB_LOG);
            perLogEnabled = ConfigUtil.getBooleanValue(PERF_LOG);
            showDomXML = ConfigUtil.getBooleanValue(DOM_DISPLAY,true);
            videoVertial = ConfigUtil.getBooleanValue(VIDEO_VERTICAL,true);

            //Create Root dir
            Util.createDir(rootDir);

            log.info("rootDir is " + rootDir);

        }catch (Exception e){
            log.error("!!!!!!Fail to read config file");
            e.printStackTrace();
        }

        return configUtil;
    }

    public static boolean isVideoVertial() {
        return videoVertial;
    }

    public static boolean isDbLogEnabled() {
        return dbLogEnabled;
    }

    public static void setDbLogEnabled(boolean dbLogEnabled) {
        ConfigUtil.dbLogEnabled = dbLogEnabled;
    }

    public static boolean isPerLogEnabled() {
        return perLogEnabled;
    }

    public static void setPerLogEnabled(boolean perLogEnabled) {
        ConfigUtil.perLogEnabled = perLogEnabled;
    }

    public static boolean boundRemoved(){
        return getBooleanValue(REMOVE_BOTTOM_BOUND,true);
    }

    public static String getIPAName(){
        return getStringValue(IOS_IPA_NAME);
    }

    public static String getRootDir(){
        return rootDir;
    }

    public static String getScreenShotDir(){
        return rootDir + File.separator + SCREEN_SHOT + File.separator;
    }

    public static String getServerIP(){
        return getStringValue("APPIUM_SERVER_IP");
    }

    public static long getDefaultWaitSec() {
        return getLongValue("DEFAULT_WAIT_SEC");
    }

    public static long getDefaultPollingIntervalSec() {
        return getLongValue("DEFAULT_POLLING_INTERVAL_SEC");
    }

    public static long getRetryCount() {
        return getLongValue("RETRY_COUNT");
    }

    public static String getPackageName() {
        return getStringValue("ANDROID_PACKAGE");
    }
    public static void setPackageName(String name) {
        setStringValue("ANDROID_PACKAGE",name);
    }

    public static String getIOSBundleName() {
        return getStringValue(IOS_BUNDLE_NAME);
    }

    public static void setIOSBundleName(String name) {
        setStringValue(IOS_BUNDLE_NAME,name);
    }

    public static String getWdaPort() {
        return getStringValue(IOS_WDA_PORT);
    }
    public static void setWdaPort(String wdaPort) {
        setStringValue(IOS_WDA_PORT,wdaPort);
    }

    public static long getClickCount() {
        return clickCount;
    }
    public static void setClickCount(long count) {
        clickCount = count;
    }

    public static long getScreenshotCount() { return getLongValue("SCREENSHOT_COUNT");}
    public static void setScreenshotCount(String count) { configItems.put("SCREENSHOT_COUNT",count);}

    public static String getActivityName() {
        return getStringValue("ANDROID_MAIN_ACTIVITY");
    }
    public static void setActivityName(String activityName) {
        setStringValue("ANDROID_MAIN_ACTIVITY",activityName);
    }

    public static String getBundleId() {
        return getStringValue(IOS_BUNDLE_ID);
    }
    public static void setBundleId(String bundleId) {
        setStringValue(IOS_BUNDLE_ID,bundleId);
    }
    public static void setIgnoreCrash(boolean val) {
        setBooleanValue(IGNORE_CRASH,val);
    }

    public static long getDepth(){
        return getLongValue(MAX_DEPTH);
    }
    public static void setMaxDepth(String depth) {
        setLongValue(MAX_DEPTH,depth);
    }

//    public static void setMonkeyRunningTime(String minutes){
//        configItems.put(ConfigUtil.MONKEY_RUNNING_TIME,Integer.parseInt(minutes));
//    }

    public static void setCrawlerRunningTime(String time){
         setLongValue(ConfigUtil.CRAWLER_RUNNING_TIME,time);
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ConfigUtil.port = port;
        setStringValue("PORT",port);
    }

    public static void setUdid(String name){
        udid = name;
    }
    public static String getUdid(){
        return udid;
    }

    public static String getDeviceName(){
        String deviceName = getStringValue("DEVICE_NAME");

        return deviceName == null? udid:deviceName;
    }

    public static String getStringValue(String key) {
        String value = String.valueOf(configItems.get(key));
        log.info("Config : " + key + " = " + value);

        return value == null || value.equals("null")? null:value.trim();
    }
    public static void setStringValue(String key,String value){
        configItems.put(key,value);
    }

    public static ArrayList<String> getListValue(String key) {
        ArrayList<String> list =(ArrayList) configItems.get(key);
        log.info("Config : " + key + " = " + list);

        return list == null? new ArrayList<>():list;
    }

    public static void setLongValue(String key, String val){
        configItems.put(key,Integer.parseInt(val));
    }

    public static long getLongValue(String key) {
        Integer value =(Integer) configItems.get(key);
        log.info("Config : " + key + " = " + value);

        return value == null? -100:value.longValue();
    }

    public static void setBooleanValue(String key, boolean val){
        configItems.put(key,val);
    }

    public static boolean getBooleanValue(String key, boolean defaultValue) {
        Boolean value = (Boolean) configItems.get(key);

        log.info("Config : " + key + " = " + value);
        return value == null? defaultValue : value;
    }

    public static boolean getBooleanValue(String key) {
        return getBooleanValue(key,false);
    }

    public static Map<String,Object> getMapValue(String key){
        Map<String,Object> map = (Map)configItems.get(key);
        log.info("Config : " + key + " = " + map);
        return map;
    }
}
