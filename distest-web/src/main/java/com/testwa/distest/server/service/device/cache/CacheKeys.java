package com.testwa.distest.server.service.device.cache;

/**
 * Created by wen on 24/06/2017.
 */
class CacheKeys {

    static final String device_user = "device.auth.%s";
    static final String user_client_login = "auth.client.login.%s";
    static final String device_client = "device.client.%s";
    static final String main_info = "main.info.%s";
    static final String subscribe_device_func = "subscribe.device.func.%s.%s";
    static final String onstart_screen_device = "onstart.screen.%s";

    // device shares 1 scope, 2 deviceId value device detail
    static final String device_share = "device.share.%s.%s";

    // web cache
    // auth project history %s = userId
    static final String user_project_history = "history.project.%s";


    private static final String device_exe_info = "device.exe.info.%s";

    /***
     * 获得保存设备执行情况的key
     * @param deviceId
     * @return
     */
    public static String deviceExeInfoKey(String deviceId){
        return String.format(device_exe_info, deviceId);
    }
}
