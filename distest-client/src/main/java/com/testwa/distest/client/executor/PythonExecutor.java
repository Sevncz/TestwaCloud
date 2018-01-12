package com.testwa.distest.client.executor;

import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.service.PythonScriptDriverService;
import com.testwa.core.service.PythonServiceBuilder;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.appium.AppiumManager;
import com.testwa.distest.client.event.ExecutorCurrentInfoNotifyEvent;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.util.Constant;
import com.testwa.distest.client.util.Http;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * python 执行器
 * 1. 下载app
 * 2. 下载脚本
 * 3. 运行脚本
 * Created by wen on 19/08/2017.
 */
@Data
public class PythonExecutor {
    private static Logger log = LoggerFactory.getLogger(PythonExecutor.class);

    private String agentWebUrl;
    private String appiumUrl;
    private PythonScriptDriverService pyService;

    private BlockingQueue<RemoteTestcaseContent> testcases;
    private BlockingQueue<Long> scripts;

    private Long appId;
    private String appPath;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private Long taskId;
    private String install;

    private boolean isStop = false;
    private Long currScript;
    private Long currTestCaseId;

    // 所有脚本的本地保存路径
    private Map<Long, String> scriptPath = new HashMap<>();


    public PythonExecutor(String agentWebUrl, String appiumUrl) {
        this.agentWebUrl = agentWebUrl;
        this.appiumUrl = appiumUrl;
    }

    public void runScripts(){
        assert StringUtils.isNotBlank(deviceId);
        assert StringUtils.isNotBlank(install);
        assert testcaseList.size() > 0;

        try {

            RemoteTestcaseContent content = this.testcases.poll();
            List<Long> scIds = content.getScriptIds();
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
                boolean status = runOneScript();
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

    private Boolean runOneScript() throws Exception {
        Long runscriptId = this.scripts.poll();
        if(runscriptId == null){
            return false;
        }

        this.currScript = runscriptId;
        AndroidApp app = new DefaultAndroidApp(new File(appPath));
        String basePackage = app.getBasePackage();
        String mainActivity = app.getMainActivity();

//        String scriptUrl = String.format(Constant.SCRIPT_URL, agentWebUrl, runscriptId);
//        String scriptPath = Http.download(scriptUrl, Constant.localScriptPath);
        String filePath = scriptPath.get(runscriptId);
        // 脚本替换
        String url = appiumUrl.replace("0.0.0.0", "127.0.0.1");
        String tempPath = replaceScriptByAndroid(filePath, appPath, basePackage, mainActivity, url);
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
                    bw.write(replaceQuotationContent(tempString, this.currScript+"", "'testSuit'"));
                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));
                    bw.write(replaceQuotationContent(tempString, this.taskId+"", "'executionTaskId'"));
                    continue;
                }

                if(tempString.contains("deviceName")){
                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));
                    bw.write(replaceQuotationContent(tempString, this.currScript+"", "'testSuit'"));
                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));
                    bw.write(replaceQuotationContent(tempString, this.taskId+"", "'executionTaskId'"));
                    continue;
                }

                //修改url
                //self.driver = webdriver.Remote('http://localhost:4730/wd/hub', desired_caps)
                if(tempString.contains("webdriver.Remote")){
                    bw.write(replaceQuotationContent(tempString, appiumUrl, null));
                    bw.write("\t\n");
                    continue;
                }
                //修改app路径
                // desired_caps['app'] = PATH('apps/ContactManager/ContactManager.apk')
                if(tempString.contains("desired_caps['app']")){
                    if(appPath.contains("\\")){
                        appPath = appPath.replaceAll("\\\\", "/");
                    }
                    bw.write(replaceQuotationContent(tempString, appPath, null));
                    bw.write("\t\n");
                    continue;
                }

                if(appPath.contains("apk")){
                    if(tempString.contains("desired_caps['appPackage']")){
                        bw.write(replaceQuotationContent(tempString, basePackage, null));
                        bw.write("\t\n");
                        continue;
                    }

                    if(tempString.contains("desired_caps['appActivity']")){
                        bw.write(replaceQuotationContent(tempString, mainActivity, null));
                        bw.write("\t\n");
                        continue;
                    }

                }else{
                    if(tempString.contains("desired_caps['platformName']")){
                        bw.write(replaceQuotationContent(tempString, "iOS", null));
                        bw.write("\t\n");
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

    public Boolean pythonIsRunning(){
        return this.pyService.isRunning();
    }

    public void stop() {
        if(!this.pyService.isRunning()){
            this.pyService.stop();
        }
        this.testcases.clear();
        this.scripts.clear();
    }

    public void downloadApp() throws DownloadFailException, IOException {
        String appUrl = String.format(Constant.APP_URL, agentWebUrl, appId);
        this.appPath = Http.download(appUrl, Constant.localAppPath);

        if(StringUtils.isBlank(appPath)){
            log.error("appPath is null, app url is [{}]", appUrl);
            throw new DownloadFailException("app download fail");
        }
    }
    public void downloadScript() throws DownloadFailException, IOException {
        for (RemoteTestcaseContent remoteTestcaseContent : this.testcaseList) {
            List<Long> scriptIds = remoteTestcaseContent.getScriptIds();
            for (Long scriptId : scriptIds) {
                if(scriptPath.containsKey(scriptId)){
                    continue;
                }
                String scriptUrl = String.format(Constant.SCRIPT_URL, agentWebUrl, scriptId);
                String saveDir = Constant.localScriptPath + File.separator + scriptId;
                String savePath = Http.download(scriptUrl, saveDir);
                if (StringUtils.isBlank(savePath)) {
                    log.error("scriptPath is null, script url is [{}]", scriptUrl);
                    throw new DownloadFailException("script download fail");
                }
                scriptPath.put(scriptId, savePath);
            }
        }
    }

    public void setAppId(Long appId){
        this.appId = appId;
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public void setTaskId(Long taskId) {
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

    public Long getCurrScript(){
        return currScript;
    }
    public Long getCurrTestCaseId(){
        return currTestCaseId;
    }

    public void notifyCurrExeInfo(){

        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new ExecutorCurrentInfoNotifyEvent(this, deviceId, taskId, currScript, currTestCaseId));
    }

}
