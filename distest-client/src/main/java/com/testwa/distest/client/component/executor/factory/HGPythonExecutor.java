package com.testwa.distest.client.component.executor.factory;import com.github.cosysoft.device.android.AndroidApp;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.testwa.core.cmd.AppInfo;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.cmd.RemoteTestcaseContent;import com.testwa.core.cmd.ScriptInfo;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.component.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.FlowResult;import com.testwa.distest.client.component.StepResult;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.ExecutorLog;import com.testwa.distest.client.download.Downloader;import com.testwa.distest.client.exception.*;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.service.GrpcClientService;import io.rpc.testwa.task.ExecutorAction;import io.rpc.testwa.task.StepRequest;import lombok.Data;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.*;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.concurrent.*;import java.util.regex.Matcher;import java.util.regex.Pattern;/** * @Program: distest * @Description: 兼容android测试 * @Author: wen * @Create: 2018-05-15 14:38 **/@Data@Slf4jpublic class HGPythonExecutor extends HGAbstractExecutor {    private String appiumUrl;    // 127.0.0.1:8080 or cloud.testwa.com    private String distestApiWeb;    // testwa//    private String distestApiName;    private UTF8CommonExecs pyexecs;    private BlockingQueue<RemoteTestcaseContent> testcases;    private BlockingQueue<ScriptInfo> scripts;    private String appLocalPath;    private String deviceId;    private AppInfo appInfo;    private ScriptInfo scriptInfo;    private List<RemoteTestcaseContent> testcaseList;    private Long taskId;    private boolean install;    private boolean isStop = false;    private ScriptInfo currScript;    private Long currTestCaseId;    // 所有脚本的本地保存路径    private Map<Long, String> scriptPath = new HashMap<>();    private Downloader downloader = new Downloader();    private GrpcClientService grpcClientService;    private boolean hasError = false;    private StringBuffer errorMsg = new StringBuffer();    private FlowResult startFlow;    private Integer cpukel;    private Integer lastFps;    private AndroidApp androidApp;    private boolean isComplete = false;    @Override    public void init(String appiumUrl, RemoteRunCommand cmd) {        this.grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        this.distestApiWeb = Config.getString("distest.api.web");        this.appiumUrl = appiumUrl;        this.appInfo = cmd.getAppInfo();        this.deviceId = cmd.getDeviceId();        this.install = cmd.getInstall();        this.taskId = cmd.getExeId();        this.testcaseList = cmd.getTestcaseList();        this.testcases = new ArrayBlockingQueue<>(cmd.getTestcaseList().size());        this.testcases.addAll(cmd.getTestcaseList());        this.cpukel = ADBCommandUtils.getCpuKel(deviceId);        try {            log.info("logcat 临时文件: {}", logcatTempFile.toString());            Files.deleteIfExists(logcatTempFile);            Files.createFile(logcatTempFile);        } catch (IOException e) {        }    }    private void stopScripts(){        this.isStop = true;    }    @ExecutorLog(action = ExecutorAction.downloadApp)    @Override    public void downloadApp() throws DownloadFailException, IOException {        String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());        this.appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileAliasName();        // 检查是否有和该app md5一致的        downloader.start(appUrl, appLocalPath);    }    @ExecutorLog(action = ExecutorAction.downloadScript)    @Override    public void downloadScript() throws DownloadFailException, IOException {        for (RemoteTestcaseContent remoteTestcaseContent : this.testcaseList) {            List<ScriptInfo> scriptInfos = remoteTestcaseContent.getScripts();            for (ScriptInfo scriptInfo : scriptInfos) {                if(scriptPath.containsKey(scriptInfo.getId())){                    continue;                }                String scriptUrl = String.format("http://%s/script/%s", distestApiWeb, scriptInfo.getPath());                String localPath = Constant.localScriptPath + File.separator + scriptInfo.getMd5() + File.separator + scriptInfo.getAliasName();                downloader.start(scriptUrl, localPath);                scriptPath.put(scriptInfo.getId(), localPath);            }        }    }    @Override    public void start(){        assert StringUtils.isNotBlank(deviceId);        assert testcaseList.size() > 0;        int initialDelay = 0;        int period = 1;        try {            androidApp = new DefaultAndroidApp(new File(appLocalPath));            logger(deviceId);            installApp();            launch();            // 性能抓取任务参数初始化            scheduledExecutor.scheduleWithFixedDelay(getPerformanceTask, initialDelay, period, TimeUnit.SECONDS);            run();            uninstallApp();            complete();        }catch (InstallAppException e){            // 安装失败            log.error("{} 任务执行错误，应用安装失败", deviceId, e.getMessage());            grpcClientService.gameover(taskId, deviceId, e.getMessage());        } catch (LaunchAppException e) {            // 启动失败            log.error("{} 任务执行错误，应用启动失败", deviceId, e.getMessage());            grpcClientService.gameover(taskId, deviceId, e.getMessage());        } catch (UninstallAppException e) {            // 卸载失败            log.error("{} 任务执行错误，应用卸载失败", deviceId, e.getMessage());            grpcClientService.gameover(taskId, deviceId, e.getMessage());        } catch (ScriptExecuteException e) {            // 执行脚本失败            log.error("{} 任务执行错误，脚本执行错误", deviceId, e.getMessage());            grpcClientService.gameover(taskId, deviceId, e.getMessage());        } finally {            loggerStop();        }    }    private Runnable getPerformanceTask = new Runnable() {        @Override        public void run() {            if(!isComplete){                try {                    String pid = ADBCommandUtils.getPid(deviceId, androidApp.getBasePackage());                    if(startFlow == null){                        startFlow = ADBCommandUtils.getFlow(deviceId, pid);                    }                    Double cpu = null;                    if(cpukel == null) {                        log.error("设备 {} 获取cpu指标错误，cpu数量为空", deviceId);                        cpukel = ADBCommandUtils.getCpuKel(deviceId);                    }                    if(cpukel != null) {                        cpu = ADBCommandUtils.cpuRate(deviceId, pid, cpukel);                    }                    // 获得累计流量                    FlowResult[] flows = ADBCommandUtils.flow(deviceId, pid, startFlow); // kb                    startFlow = flows[0];                    FlowResult resultFlow = flows[1];                    Integer mem = ADBCommandUtils.mem(deviceId, androidApp.getBasePackage()); // kb                    Integer bat = ADBCommandUtils.battery(deviceId);                    Integer fps = ADBCommandUtils.fps(deviceId, androidApp.getBasePackage());                    if(fps == null){                        if (lastFps != null){                            fps = lastFps;                        }else{                            fps = 0;                        }                    }else{                        lastFps = fps;                    }                    log.debug("mem: {}, bat: {}, cpu: {}, fps: {} flow: {}", mem, bat, cpu, fps, resultFlow);                    grpcClientService.savePreformance(cpu, mem, bat, fps, resultFlow, taskId, deviceId);                } catch (Exception e) {                    log.error("设备 {} 获取指标异常", deviceId, e);                }            }        }    };    @ExecutorLog(action = ExecutorAction.installApp)    @Override    public void installApp() throws InstallAppException {        if(ADBCommandUtils.isInstalledBasepackage(deviceId, androidApp.getBasePackage())){            ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        }        StepResult result = ADBCommandUtils.installApp(deviceId, appLocalPath, 300*1000L);        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new InstallAppException(result.getErrormsg());        }    }    private void sendStepRequest(StepResult result) {        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(taskId)                .setDeviceId(deviceId)                .setAction(result.getAction())                .setStatus(result.getStatus())                .setRuntime(result.getTotalTime())                .build();        grpcClientService.saveStep(request);    }    @ExecutorLog(action = ExecutorAction.launch)    @Override    public void launch() throws LaunchAppException {        StepResult result = ADBCommandUtils.launcherApp(deviceId, appLocalPath);        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new LaunchAppException(result.getErrormsg());        }    }    @ExecutorLog(action = ExecutorAction.run)    @Override    public void run() throws ScriptExecuteException {        try {            androidApp = new DefaultAndroidApp(new File(appLocalPath));            RemoteTestcaseContent content = this.testcases.poll();            if(content == null){                log.error("没有脚本可执行");                return;            }            List<ScriptInfo> scIds = content.getScripts();            this.scripts = new ArrayBlockingQueue<>(scIds.size());            this.scripts.addAll(scIds);            this.currTestCaseId = content.getTestcaseId();            for(;;){                if(isStop){                    break;                }                if(testcases.isEmpty() && scripts.isEmpty()){                    break;                }                if(this.scripts.isEmpty()){                    content = this.testcases.poll();                    scIds = content.getScripts();                    this.scripts = new ArrayBlockingQueue<>(scIds.size());                    this.scripts.addAll(scIds);                }                ScriptInfo runscript = this.scripts.poll();                runOneScript(runscript);                if(isComplete){                    log.info("脚本全部执行完毕");                    break;                }            }        }catch (Exception e) {            log.error("Execute script error", e);            hasError = true;            errorMsg.append(e.getMessage());        }        if(hasError) {            throw new ScriptExecuteException(errorMsg.toString());        }    }    /**     * 需要代理，必须是public方法     * @return     * @throws Exception     */    public void runOneScript(ScriptInfo runscript) throws Exception {        if(runscript == null){            isComplete = true;            return;        }        isComplete = false;        log.debug("run one script {}, deviceId {}", runscript.toString(), this.deviceId);        this.currScript = runscript;        String filePath = scriptPath.get(runscript.getId());        // 脚本替换        String url = appiumUrl.replace("0.0.0.0", "127.0.0.1");        String tempPath = this.replaceScriptByAndroid(filePath, appLocalPath, url);        log.debug("temp script path is [{}]", tempPath);        // 执行脚本        startPy(tempPath);    }    public String replaceScriptByAndroid(String localPath,                                         String appPath,                                         String appiumUrl) throws Exception {        String basePackage = appInfo.getPackageName();        String mainActivity = appInfo.getActivity();        File file = new File(localPath);        BufferedReader reader = null;        BufferedWriter bw = null;        Path transfDir = Paths.get(Constant.localScriptTmpPath, deviceId);        if(!Files.exists(transfDir)){            Files.createDirectory(transfDir);        }        File localFile = new File(localPath);        Path transfPath = Paths.get(transfDir.toString(), localFile.getName());        try {            File f = transfPath.toFile();            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));            String tempString;            int line = 1;            // 一次读入一行，直到读入null为文件结束            while ((tempString = reader.readLine()) != null) {                // 显示行号                log.debug("line [{}] : [{}]" , line, tempString);                line++;                if(tempString.contains("udid")){                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));                    bw.write(replaceQuotationContent(tempString, this.taskId+"", "'executionTaskId'"));                    continue;                }                if(tempString.contains("deviceName")){                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));                    bw.write(replaceQuotationContent(tempString, this.taskId+"", "'executionTaskId'"));                    continue;                }                //修改url                //self.driver = webdriver.Remote('http://localhost:4730/wd/hub', desired_caps)                if(tempString.contains("webdriver.Remote")){                    bw.write(replaceQuotationContent(tempString, appiumUrl, null));                    bw.write("\t\n");                    continue;                }                //修改app路径                // desired_caps['app'] = PATH('apps/ContactManager/ContactManager.apk')                if(tempString.contains("desired_caps['app']")){                    if(appPath.contains("\\")){                        appPath = appPath.replaceAll("\\\\", "/");                    }                    bw.write(replaceQuotationContent(tempString, appPath, null));                    bw.write("\t\n");                    continue;                }                if(appPath.endsWith(".apk")){                    if(tempString.contains("desired_caps['appPackage']")){                        bw.write(replaceQuotationContent(tempString, basePackage, null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['appActivity']")){                        bw.write(replaceQuotationContent(tempString, mainActivity, null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['platformName']")){                        bw.write(replaceQuotationContent(tempString, "Android", null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['platformVersion']")){                        AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(deviceId);                        String version = ad.runAdbCommand("shell getprop ro.build.version.release");                        bw.write(replaceQuotationContent(tempString, version, null));                        bw.write("\t\n");                        continue;                    }                }else{                    if(tempString.contains("desired_caps['platformName']")){                        bw.write(replaceQuotationContent(tempString, "iOS", null));                        bw.write("\t\n");                        continue;                    }                }                bw.write(tempString + "\t\n");            }        } catch (IOException e) {            log.error("Modify script error, localPath: [{}], transfPath: [{}]", localPath, transfPath , e);        } finally {            if (reader != null) {                try {                    reader.close();                } catch (IOException e1) {                    log.error("Close BufferedReader in method replaceScriptByAndroid ", e1);                }            }            if(bw != null){                bw.close();            }        }        return transfPath.toString();    }    private String replaceQuotationContent(String srcStr, String replaceStr, String key){        String[] srcStrs = srcStr.split("=");        if(srcStrs.length >= 2){            String needReplaceStr = srcStrs[srcStrs.length-1];            String needReplaceKey = srcStrs[0];            String rule = "\"[^\"]*\"|'[^']*'";            Pattern p = Pattern.compile(rule);            Matcher mKey = p.matcher(needReplaceKey);            if(StringUtils.isNotEmpty(key)){                Matcher k = p.matcher(key);                if(!k.find()){                    key = "'" + key + "'";                }                if(mKey.find()){                    srcStrs[0] = mKey.replaceFirst(key);                }            }            Matcher m = p.matcher(needReplaceStr);            if(m.find()){                String newStr = m.replaceFirst("'" + replaceStr + "'").trim();                StringBuffer sb = new StringBuffer();                for(int i=0;i<srcStrs.length - 1;i++){                    sb.append(srcStrs[i]).append("=");                }                sb.append(newStr);                sb.append("\t\n");                return sb.toString();            }        }        return srcStr;    }    public void startPy(String pyPath) {        CommandLine commandLine = new CommandLine("python");        commandLine.addArgument(pyPath);        pyexecs = new UTF8CommonExecs(commandLine);        // 设置最大执行时间，5分钟        pyexecs.setTimeout(5*60*1000L);        try {            pyexecs.exec();            String output = pyexecs.getOutput();        } catch (IOException e) {            String error = pyexecs.getError();            log.error("Python 脚本执行错误 \n {}", error, e);            hasError = true;            errorMsg.append(error).append("\n");        }    }    @ExecutorLog(action = ExecutorAction.uninstallApp)    @Override    public void uninstallApp() throws UninstallAppException {        StepResult result = ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new UninstallAppException(result.getErrormsg());        }    }    @ExecutorLog(action = ExecutorAction.complete)    @Override    public void complete() {        this.grpcClientService.logcatFileUpload(logcatTempFile, taskId, deviceId);        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(taskId)                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.complete)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }    @ExecutorLog(action = ExecutorAction.stop)    @Override    public void stop() {        if(this.pyexecs != null){            this.pyexecs.destroy();        }        if(this.testcases != null){            this.testcases.clear();        }        if(this.scripts != null){            this.scripts.clear();        }        if(this.logexecs != null){            this.logexecs.destroy();        }        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(taskId)                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.stop)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }}