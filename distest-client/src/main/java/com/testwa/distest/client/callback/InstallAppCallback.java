package com.testwa.distest.client.callback;import com.alibaba.fastjson.JSON;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.google.protobuf.ByteString;import com.testwa.core.cmd.AppInfo;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.download.Downloader;import com.testwa.distest.client.exception.DownloadFailException;import lombok.extern.slf4j.Slf4j;import java.io.File;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class InstallAppCallback implements ICallBack<String> {    private DeviceClient client;    public InstallAppCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String command = bytes.toStringUtf8();        log.debug(client+ ":callback-log " + command);        Thread t = new Thread(new InstallRunner(command));        t.start();    }    private class InstallRunner implements Runnable{        private String cmdJson;        public InstallRunner(String cmdJson) {            this.cmdJson = cmdJson;        }        @Override        public void run() {            log.info("设备 {} 开始下载及安装App", client.getClientId());            AppInfo appInfo = JSON.parseObject(cmdJson, AppInfo.class);            AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(client.getClientId());            String distestApiWeb = Config.getString("distest.api.web");            String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());            String appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileName();            // 检查是否有和该app md5一致的            try {                Downloader d = new Downloader();                d.start(appUrl, appLocalPath);            } catch (DownloadFailException | IOException e) {                e.printStackTrace();            }            ADBCommandUtils.installApp(client.getClientId(), appLocalPath);            ADBCommandUtils.launcherApp(client.getClientId(), appLocalPath);        }    }}