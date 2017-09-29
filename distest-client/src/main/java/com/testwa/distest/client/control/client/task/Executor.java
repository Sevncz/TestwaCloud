package com.testwa.distest.client.control.client.task;

import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.core.service.PythonScriptDriverService;
import com.testwa.core.service.PythonServiceBuilder;
import com.testwa.core.utils.Identities;
import com.testwa.distest.client.appium.manager.CustomServerFlag;
import com.testwa.distest.client.control.client.Clients;
import com.testwa.distest.client.control.port.AppiumPortProvider;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.util.Constant;
import com.testwa.distest.client.util.Http;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.AndroidServerFlag;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.rpc.testwa.task.CurrentExeInfoRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 19/08/2017.
 */
public class Executor {
    private static Logger log = LoggerFactory.getLogger(Executor.class);
    private int port;
    private int bootstrapPort;
    private String agentWebUrl;
    private AppiumDriverLocalService service;
    private PythonScriptDriverService pyService;

    private BlockingQueue<RemoteTestcaseContent> testcases;
    private BlockingQueue<String> scripts;

    private String appId;
    private String appPath;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private String taskId;
    private String install;

    private boolean isStop = false;
    private String currScript;
    private String currTestCaseId;


    public Executor(String nodePath, String appiumPath, String agentWebUrl, String clientWebUrl) throws IOException {
        this.agentWebUrl = agentWebUrl;
        this.port = AppiumPortProvider.pullPort();
        this.bootstrapPort = AppiumPortProvider.pullPort();
        String appiumlogPath = getAppiumlogPath(this.port + "");

        AppiumServiceBuilder builder =
                new AppiumServiceBuilder()
                        .usingDriverExecutable(new File(nodePath))
                        .withAppiumJS(new File(appiumPath))
                        .withIPAddress("127.0.0.1")
                        .withArgument(GeneralServerFlag.LOG_LEVEL, "info").withLogFile(new File(appiumlogPath))
                        .withArgument(AndroidServerFlag.SUPPRESS_ADB_KILL_SERVER)
                        .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER, Integer.toString(bootstrapPort))
                        .withArgument(CustomServerFlag.SCREEN_PATH, Constant.localScreenshotPath)
                        .withArgument(CustomServerFlag.FOR_PORTAL, clientWebUrl)
                        .usingPort(port);
        service = builder.build();
        service.start();
    }

    public void runScripts(){
        assert StringUtils.isNotBlank(appId);
        assert StringUtils.isNotBlank(deviceId);
        assert StringUtils.isNotBlank(taskId);
        assert StringUtils.isNotBlank(install);
        assert testcaseList.size() > 0;

        try {

            AndroidApp app = new DefaultAndroidApp(new File(appPath));
            String basePackage = app.getBasePackage();
            String mainActivity = app.getMainActivity();


            RemoteTestcaseContent content = this.testcases.poll();
            List<String> scIds = content.getScriptIds();
            this.scripts = new ArrayBlockingQueue<>(scIds.size());
            this.scripts.addAll(scIds);
            this.currTestCaseId = content.getTestcaseId();

            for(;;){
                if(isStop){
                    break;
                }
                log.info("run one script {}, deviceId {}", this.currScript, this.deviceId);

                if(this.pyService != null && this.pyService.isRunning()){
                    Thread.sleep(1000);
                    continue;
                }

                if(testcases.isEmpty() && scripts.isEmpty()){
                    break;
                }

                if(this.scripts.isEmpty()){
                    content = this.testcases.poll();
                    scIds = content.getScriptIds();
                    this.scripts = new ArrayBlockingQueue<>(scIds.size());
                    this.scripts.addAll(scIds);
                }
                boolean status = runOneScript(appPath, basePackage, mainActivity);
                if(!status){
                    log.info("Complete All !");
                    break;
                }
            }
        }catch (Exception e) {
            log.error("Execute script error", e);
        }finally {
            // upload appium log
            Path appiumlogDir = Paths.get(Constant.localAppiumLogPath, deviceId.replaceAll("\\W", "_"));
            log.info("Upload appium log to server, path: {}", appiumlogDir.toString());
            String appiumlogUploadUrl = String.format("%s/device/receive/appiumlog", agentWebUrl);
            sendLogsToServer(appiumlogDir, appiumlogUploadUrl);

            //upload logcat log
            Path logcatDir = Paths.get(Constant.localLogcatPath, deviceId.replaceAll("\\W", "_"));
            log.info("Upload logcat to server, path: {}", logcatDir.toString());
            String logcatUploadUrl = String.format("%s/device/receive/logcat", agentWebUrl);
            sendLogsToServer(logcatDir, logcatUploadUrl);
        }
    }

    private void stopScripts(){
        this.isStop = true;
    }

    private void sendLogsToServer(Path dirPath, String uploadUrl) {

    }

    private Boolean runOneScript(String appPath, String basePackage, String mainActivity) throws Exception {
        String runscriptId = this.scripts.poll();
        if(StringUtils.isBlank(runscriptId)){
            return false;
        }

        this.currScript = runscriptId;


        String scriptUrl = String.format(Constant.SCRIPT_URL, agentWebUrl, runscriptId);

        String scriptPath = Http.download(scriptUrl, Constant.localAppPath);
        // 脚本替换
        String url = this.service.getUrl().toString().replace("0.0.0.0", "127.0.0.1");
        String tempPath = replaceScriptByAndroid(scriptPath, appPath, basePackage, mainActivity, url);
        log.info("temp script path is [{}]", tempPath);

        // 执行脚本
        this.pyService = new PythonServiceBuilder()
                .withPyScript(new File(tempPath))
                .build();
        this.pyService.start();
        log.info("python script start......");

        this.notifyCurrExeInfo();

        return true;
    }

    private String replaceScriptByAndroid(String localPath,
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
                log.debug("line [{}] : [{}]" , line, tempString);
                line++;

                if(tempString.contains("udid")){
                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));
                    bw.write(replaceQuotationContent(tempString, this.currScript, "'testSuit'"));
                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId, "'testcaseId'"));
                    bw.write(replaceQuotationContent(tempString, this.taskId, "'executionTaskId'"));
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
            log.error("Modify script error, localPath: [{}], transfPath: [{}]", localPath, transfPath , e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    log.error("Close BufferedReader in method replaceScriptByAndroid ", e1);
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

    private String getAppiumlogPath(String port) throws IOException {
        Path appiumlogDir = Paths.get(Constant.localAppiumLogPath, port);
        if(!Files.exists(appiumlogDir)){
            Files.createDirectories(appiumlogDir);
        }
        return Paths.get(appiumlogDir.toString(), String.format("appium_%s.log", Identities.randomLong())).toString();
    }

    public Boolean appiumIsRunning(){
        return this.service.isRunning();
    }

    public void appiumStart() {
        if(!this.service.isRunning()){
            this.service.start();
        }
    }

    public Boolean pythonIsRunning(){
        return this.pyService.isRunning();
    }

    public void destory(){
        this.service.stop();
    }

    public void pythonStop() {
        if(!this.pyService.isRunning()){
            this.pyService.stop();
        }
        this.testcases.clear();
        this.scripts.clear();
    }

    public void setAppId(String appId){
        this.appId = appId;
        String appUrl = String.format(Constant.APP_URL, agentWebUrl, appId);
        this.appPath = Http.download(appUrl, Constant.localAppPath);

        if(StringUtils.isBlank(appPath)){
            log.error("appPath is null, app url is [{}]", appUrl);
        }

    }
    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setInstall(String install) {
        this.install = install;
    }

    public void setTestcaseList(List<RemoteTestcaseContent> testcaseList) {
        this.testcaseList = testcaseList;
        this.testcases = new ArrayBlockingQueue<>(testcaseList.size());
        this.testcases.addAll(testcaseList);
    }

    public String getCurrScript(){
        return currScript;
    }
    public String getCurrTestCaseId(){
        return currTestCaseId;
    }

    public void reset() {
        this.scripts = null;
        this.testcases = null;
        this.pyService = null;
        this.appId = null;
        this.taskId = null;
        this.currScript = null;
        this.currTestCaseId = null;
        this.testcaseList = null;
        this.deviceId = null;
        this.install = null;
        this.isStop = false;
    }

    public void notifyCurrExeInfo(){

        CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()
                .setDeviceId(this.deviceId)
                .setExeId(this.taskId)
                .setScriptId(this.currScript)
                .setTestcaseId(this.currTestCaseId)
                .setToken(UserInfo.token)
                .build();
//        TaskServiceGrpc.newFutureStub(managedChannel).currExeInfo(request);
        Clients.taskService().currExeInfo(request);
    }

}
