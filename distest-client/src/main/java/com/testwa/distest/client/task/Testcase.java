package com.testwa.distest.client.task;

import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.appium.manager.AppiumCache;
import com.testwa.distest.client.appium.manager.AppiumParallelTest;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.core.service.PythonScriptDriverService;
import com.testwa.core.service.PythonServiceBuilder;
import com.testwa.distest.client.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/8/28.
 */
public class Testcase {
    private static Logger LOG = LoggerFactory.getLogger(Testcase.class);
    public static final String feedback_report_sdetail = WebsocketEvent.FB_REPORT_SDETAIL;

    private String appId;
    private String serial;
    private List<String> scriptIds;
    private String reportDetailId;
    private String install;

    private AndroidDevice androidDevice;

    private AppiumParallelTest ap;
    private PythonScriptDriverService pyDS;

    private BlockingQueue<String> task;

    private String currScript;
    private String agentWebUrl;

    private HttpService httpService;

    public Testcase(String appId, String serial, List<String> scriptIds, String reportDetailId, String install, String agentWebUrl, HttpService httpService) {
        this.appId = appId;
        this.serial = serial;
        this.scriptIds = scriptIds;
        this.reportDetailId = reportDetailId;
        this.install = install;
        this.androidDevice = AndroidHelper.getInstance().getAndroidDevice(serial);
        this.task = new ArrayBlockingQueue<>(scriptIds.size());
        this.task.addAll(scriptIds);

        this.agentWebUrl = agentWebUrl;

        this.httpService = httpService;
    }

    public void runAppium() throws Exception {
        if(this.ap == null){
            this.ap = new AppiumParallelTest();
            if(this.androidDevice != null){
                this.ap.startAppiumServer(serial, install, reportDetailId);
            }
        }
        // create logcat dir
        Path logcatPath = Paths.get(Constant.localLogcatPath, this.serial.replaceAll("\\W", "_"));
        if(!Files.exists(logcatPath)){
            Files.createDirectories(logcatPath);
        }
    }

    public void runScripts(){

        new Thread(() -> {
            try {
                String appUrl = String.format(Constant.APP_URL, agentWebUrl, appId);
                String appPath = Http.download(appUrl, Constant.localAppPath);
                int i = 0;
                while(!ap.appiumMan.appiumDriverLocalService.isRunning()){
                    try {
                        if(i > 500){
                            LOG.error("Appium start error, waite 50s was not started.", ap.appiumMan.appiumDriverLocalService.getUrl());
                            return;
                        }
                        Thread.sleep(100);
                        i++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(StringUtils.isBlank(appPath)){
                    LOG.error("appPath is null, app url is [{}]", appUrl);
                    return;
                }
                AndroidApp app = new DefaultAndroidApp(new File(appPath));
                String basePackage = app.getBasePackage();
                String mainActivity = app.getMainActivity();
                do{
                    if(this.getPyDS() != null){
                        if(this.getPyDS().isRunning()){
                            Thread.sleep(10 * 1000);
                            continue;
                        }else{
                            Boolean s = this.getPyDS().isSuccess();
                            if(s != null && !s){
                                LOG.error("python excute error", this.getPyDS().getStdOut());
                                this.getPyDS().stop();
                            }
                            Agent.ReportSdetailFeedback rs = Agent.ReportSdetailFeedback.newBuilder()
                                    .setReportDetailId(this.reportDetailId)
                                    .setScriptId(this.currScript)
                                    .setType(Agent.ReportSdetailType.end).build();
                            MainSocket.getSocket().emit(feedback_report_sdetail, rs.toByteArray());
                        }
                    }
                    boolean status = runOneScript(appPath, basePackage, mainActivity);
                    if(!status){
                        LOG.info("Complete All !");
                        break;
                    }
                }while(this.getPyDS() != null);
                LOG.info("Testcase task was done, deviceId: {}, appid: {}", this.getSerial(), this.getAppId());
            }catch (Exception e) {
                LOG.error("Execute script error", e);
            }finally {

                this.ap.appiumMan.appiumDriverLocalService.stop();

                AppiumCache.apt.remove(serial);
                AppiumCache.url.remove(serial);
                AppiumCache.device_running.remove(serial);

                // upload appium log
                Path appiumLogDir = Paths.get(Constant.localAppiumLogPath, serial.replaceAll("\\W", "_"));
                LOG.info("Upload appium log to server, path: {}", appiumLogDir.toString());
                String appiumLogUploadUrl = String.format("%s/device/receive/appiumlog", agentWebUrl);
                sendLogsToServer(appiumLogDir, appiumLogUploadUrl);

                //upload logcat log
                Path logcatDir = Paths.get(Constant.localLogcatPath, serial.replaceAll("\\W", "_"));
                LOG.info("Upload logcat to server, path: {}", logcatDir.toString());
                String logcatUploadUrl = String.format("%s/device/receive/logcat", agentWebUrl);
                sendLogsToServer(logcatDir, logcatUploadUrl);
            }
        }).start();
    }

    private void sendLogsToServer(Path dirPath, String uploadUrl) {
        try {
            Files.newDirectoryStream(
                    dirPath,
                    entry -> {
                        return entry.toString().endsWith(".log");
                    })
                    .forEach(name -> httpService.postProtoFile(uploadUrl, name, serial, reportDetailId));
        } catch (IOException e) {
            LOG.error("Send appium log file error, path: [{}]", dirPath.toString(), e);
        }
    }

    private Boolean runOneScript(String appPath, String basePackage, String mainActivity) throws Exception {
        String runscriptId = this.task.poll();
        if(StringUtils.isBlank(runscriptId)){
            return false;
        }

        this.currScript = runscriptId;

        String scriptUrl = String.format(Constant.SCRIPT_URL, agentWebUrl, runscriptId);

        String scriptPath = Http.download(scriptUrl, Constant.localAppPath);
        // 脚本替换
        String url = ap.appiumMan.getAppiumUrl().toString().replace("0.0.0.0", "127.0.0.1");
        String tempPath = replaceScriptByAndroid(scriptPath, this.reportDetailId, runscriptId, ap.device_udid, appPath, basePackage, mainActivity, url);
        LOG.info("temp script path is [{}]", tempPath);

        // 执行脚本
        PythonScriptDriverService driverService = new PythonServiceBuilder()
                .withPyScript(new File(tempPath))
                .build();
        this.setPyDS(driverService);
        this.getPyDS().start();

        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        Agent.ReportSdetailFeedback rs = Agent.ReportSdetailFeedback.newBuilder()
                .setReportDetailId(this.reportDetailId)
                .setScriptId(runscriptId)
                .setMatchineName(hostname)
                .setType(Agent.ReportSdetailType.start).build();
        MainSocket.getSocket().emit(feedback_report_sdetail, rs.toByteArray());

        return true;
    }

    private String replaceScriptByAndroid(String localPath,
                                          String reportDetailId,
                                          String runscriptId,
                                          String deviceId,
                                          String appPath,
                                          String basePackage,
                                          String mainActivity,
                                          String appiumUrl) throws Exception {
        File file = new File(localPath);
        BufferedReader reader = null;
        BufferedWriter bw = null;

        String transfPath = Constant.localScriptTmpPath + localPath.substring(localPath.lastIndexOf(File.separator), localPath.length());
        try {
            File f = new File(transfPath);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                LOG.debug("line [{}] : [{}]" , line, tempString);
                line++;

                if(tempString.contains("deviceName")){
                    bw.write(replaceQuotationContent(tempString, deviceId, null));
                    bw.write(replaceQuotationContent(tempString, runscriptId, "'testSuit'"));
                    bw.write(replaceQuotationContent(tempString, reportDetailId, "'testcaseId'"));
                    continue;
                }

                //修改url
                //self.driver = webdriver.Remote('http://localhost:4730/wd/hub', desired_caps)
                if(tempString.contains("webdriver.Remote")){
                    bw.write(replaceQuotationContent(tempString, appiumUrl, null));
                    continue;
                }
                //修改app路径
                // desired_caps['app'] = PATH('apps/ContactManager/ContactManager.apk')
                if(tempString.contains("desired_caps['app']")){
                    if(appPath.contains("\\")){
                        appPath = appPath.replaceAll("\\\\", "/");
                    }
                    bw.write(replaceQuotationContent(tempString, appPath, null));
                    continue;
                }

                if(appPath.endsWith("apk")){
                    // 需要判断是否是兼容测试
//                    if(tempString.contains("desired_caps['appPackage']")){
//                        bw.write(replaceQuotationContent(tempString, basePackage, null));
//                        continue;
//                    }
//
//                    if(tempString.contains("desired_caps['appActivity']")){
//                        bw.write(replaceQuotationContent(tempString, mainActivity, null));
//                        continue;
//                    }

                }else{
                    if(tempString.contains("desired_caps['platformName']")){
                        bw.write(replaceQuotationContent(tempString, "iOS", null));
                        continue;
                    }
                }

                bw.write(tempString + "\t\n");
            }
        } catch (IOException e) {
            LOG.error("Modify script error, localPath: [{}], transfPath: [{}]", localPath, transfPath , e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LOG.error("Close BufferedReader in method replaceScriptByAndroid ", e1);
                }
            }
            if(bw != null){
                bw.close();
            }
        }
        return transfPath;
    }

    private String replaceQuotationContent(String srcStr, String replaceStr, String key){
        String[] srcStrs = srcStr.split("=");
        if(srcStrs.length >= 2){
            String needReplaceStr = srcStrs[srcStrs.length-1];
            String needReplaceKey = srcStrs[0];

            String rule = "\"[^\"]*\"|'[^']*'";
            Pattern p = Pattern.compile(rule);
            Matcher mKey = p.matcher(needReplaceKey);
            if(StringUtils.isNotEmpty(key)){
                Matcher k = p.matcher(key);
                if(!k.find()){
                    key = "'" + key + "'";
                }
                if(mKey.find()){
                    srcStrs[0] = mKey.replaceFirst(key);
                }
            }
            Matcher m = p.matcher(needReplaceStr);
            if(m.find()){
                String newStr = m.replaceFirst("'" + replaceStr + "'").trim();
                StringBuffer sb = new StringBuffer();
                for(int i=0;i<srcStrs.length - 1;i++){
                    sb.append(srcStrs[i]).append("=");
                }
                sb.append(newStr);
                sb.append("\t\n");
                return sb.toString();
            }
        }
        return srcStr;
    }

    public String getAppId() {
        return appId;
    }

    public List<String> getScriptIds() {
        return scriptIds;
    }

    public AndroidDevice getAndroidDevice() {
        return androidDevice;
    }

    public AppiumParallelTest getAp() {
        return ap;
    }

    public PythonScriptDriverService getPyDS() {
        return pyDS;
    }

    public void setPyDS(PythonScriptDriverService pyDS) {
        this.pyDS = pyDS;
    }

    public String getSerial() {
        return serial;
    }

}
