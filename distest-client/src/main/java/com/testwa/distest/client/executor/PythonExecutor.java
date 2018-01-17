package com.testwa.distest.client.executor;

import com.github.cosysoft.device.android.AndroidApp;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteTestcaseContent;
import com.testwa.core.cmd.ScriptInfo;
import com.testwa.core.service.PythonScriptDriverService;
import com.testwa.core.service.PythonServiceBuilder;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.event.ExecutorCurrentInfoNotifyEvent;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.util.Constant;
import com.testwa.distest.client.util.Http;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import sun.font.Script;

import java.io.*;
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
@Slf4j
public class PythonExecutor {
    private String appiumUrl;
    // 127.0.0.1:8080 or cloud.testwa.com
    private String distestApiWeb;
    // testwa
    private String distestApiName;

    private PythonScriptDriverService pyService;

    private BlockingQueue<RemoteTestcaseContent> testcases;
    private BlockingQueue<ScriptInfo> scripts;

    private String appLocalPath;

    private String deviceId;
    private AppInfo appInfo;
    private ScriptInfo scriptInfo;

    private List<RemoteTestcaseContent> testcaseList;
    private Long taskId;

    private String install;

    private boolean isStop = false;
    private ScriptInfo currScript;
    private Long currTestCaseId;

    // 所有脚本的本地保存路径
    private Map<Long, String> scriptPath = new HashMap<>();

    public PythonExecutor(String distestApiWeb, String distestApiName, String appiumUrl) {
        this.appiumUrl = appiumUrl;
        this.distestApiWeb = distestApiWeb;
        this.distestApiName = distestApiName;
    }

    public void runScripts(){
        assert StringUtils.isNotBlank(deviceId);
        assert StringUtils.isNotBlank(install);
        assert testcaseList.size() > 0;

        try {

            RemoteTestcaseContent content = this.testcases.poll();
            List<ScriptInfo> scIds = content.getScripts();
            this.scripts = new ArrayBlockingQueue<>(scIds.size());
            this.scripts.addAll(scIds);
            this.currTestCaseId = content.getTestcaseId();

            for(;;){
                if(isStop){
                    break;
                }

                if(this.pyService != null && this.pyService.isRunning()){
                    Thread.sleep(1000);
                    continue;
                }

                if(testcases.isEmpty() && scripts.isEmpty()){
                    break;
                }

                if(this.scripts.isEmpty()){
                    content = this.testcases.poll();
                    scIds = content.getScripts();
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
        }
    }

    private void stopScripts(){
        this.isStop = true;
    }

    private Boolean runOneScript() throws Exception {
        ScriptInfo runscript = this.scripts.poll();
        if(runscript == null){
            return false;
        }
        log.info("run one script {}, deviceId {}", runscript.toString(), this.deviceId);

        this.currScript = runscript;
        AndroidApp app = new DefaultAndroidApp(new File(appLocalPath));
        String basePackage = app.getBasePackage();
        String mainActivity = app.getMainActivity();

//        String scriptUrl = String.format(Constant.SCRIPT_URL, agentWebUrl, runscriptId);
//        String scriptPath = Http.download(scriptUrl, Constant.localScriptPath);
        String filePath = scriptPath.get(runscript.getId());
        // 脚本替换
        String url = appiumUrl.replace("0.0.0.0", "127.0.0.1");
        String tempPath = replaceScriptByAndroid(filePath, appLocalPath, basePackage, mainActivity, url);
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
                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));
                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));
                    bw.write(replaceQuotationContent(tempString, this.taskId+"", "'executionTaskId'"));
                    continue;
                }

                if(tempString.contains("deviceName")){
                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));
                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));
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
                if(tempString.contains("desired_caps['platformVersion']")){
                    AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(deviceId);
                    String version = ad.runAdbCommand("shell getprop ro.build.version.release");
                    bw.write(replaceQuotationContent(tempString, version, null));
                    bw.write("\t\n");
                    continue;
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

    public void stop() {
        if(this.pyService != null){
            this.pyService.stop();
        }
        this.testcases.clear();
        this.scripts.clear();
    }

    public void downloadApp() throws DownloadFailException, IOException {
        String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());
        this.appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getAliasName();
        // 检查是否有和该app md5一致的
        Http.download(appUrl, appLocalPath);
    }
    public void downloadScript() throws DownloadFailException, IOException {
        for (RemoteTestcaseContent remoteTestcaseContent : this.testcaseList) {
            List<ScriptInfo> scriptInfos = remoteTestcaseContent.getScripts();
            for (ScriptInfo scriptInfo : scriptInfos) {
                if(scriptPath.containsKey(scriptInfo.getId())){
                    continue;
                }
                String scriptUrl = String.format("http://%s/script/%s", distestApiWeb, scriptInfo.getPath());
                String localPath = Constant.localScriptPath + File.separator + scriptInfo.getMd5() + File.separator + scriptInfo.getAliasName();
                Http.download(scriptUrl, localPath);
                scriptPath.put(scriptInfo.getId(), localPath);
            }
        }
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public void setAppInfo(AppInfo appInfo){
        this.appInfo = appInfo;
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

    public ScriptInfo getCurrScript(){
        return currScript;
    }
    public Long getCurrTestCaseId(){
        return currTestCaseId;
    }

    public void notifyCurrExeInfo(){

        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new ExecutorCurrentInfoNotifyEvent(this, deviceId, taskId, currScript.getId(), currTestCaseId));
    }

}
