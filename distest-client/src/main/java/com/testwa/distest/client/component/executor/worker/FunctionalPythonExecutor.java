package com.testwa.distest.client.component.executor.worker;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.cmd.RemoteTestcaseContent;import com.testwa.core.cmd.ScriptInfo;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.executor.ExecutorLog;import com.testwa.distest.client.component.executor.TestTaskListener;import com.testwa.distest.client.component.executor.uiautomator2.AppiumUi2ServerDaemon;import com.testwa.distest.client.component.executor.uiautomator2.Ui2ServerForAppium;import com.testwa.distest.client.exception.*;import com.testwa.distest.client.model.UserInfo;import io.rpc.testwa.task.ExecutorAction;import io.rpc.testwa.task.StepRequest;import lombok.Data;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.*;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.concurrent.*;import java.util.regex.Matcher;import java.util.regex.Pattern;/** * @Program: distest * @Description: 回归测试 * @Author: wen * @Create: 2018-05-15 14:38 **/@Data@Slf4jpublic class FunctionalPythonExecutor extends FunctionalAbstractExecutor {    private String appiumUrl;    private UTF8CommonExecs pyexecs;    private BlockingQueue<RemoteTestcaseContent> testcases;    private BlockingQueue<ScriptInfo> scripts;    private ScriptInfo scriptInfo;    private List<RemoteTestcaseContent> testcaseList;    private Long taskCode;    private boolean isStop = false;    private ScriptInfo currScript;    private Long currTestCaseId;    // 所有脚本的本地保存路径    private Map<Long, String> scriptPath = new HashMap<>();    private boolean hasError = false;    private StringBuffer errorMsg = new StringBuffer();    private boolean isComplete = false;    private Thread ui2Thread;    @Override    public void init(String appiumUrl, RemoteRunCommand cmd, TestTaskListener listener) {        this.appiumUrl = appiumUrl;        this.taskCode = cmd.getTaskCode();        this.testcaseList = cmd.getTestcaseList();        this.testcases = new ArrayBlockingQueue<>(cmd.getTestcaseList().size());        this.testcases.addAll(cmd.getTestcaseList());        super.init(cmd, listener);    }    private void stopScripts(){        this.isStop = true;    }    @Override    public void start(){        try {            downloadApp();            downloadScript();            loggerStart();            installApp();            launch();            run();            uninstallApp();            complete();        }catch (InstallAppException | LaunchAppException | UninstallAppException | TestcaseRunningException e){            // 安装失败            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "没有错误信息";            }            log.error("设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(taskCode, deviceId, error);        } catch (Exception e) {            // 未知错误            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "未知错误信息";            }            log.error("设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error, e);            grpcClientService.gameover(taskCode, deviceId, error);        } finally {            cleanThread();        }    }    @ExecutorLog(action = ExecutorAction.downloadApp)    public void downloadApp() {        super.downloadApp();    }    @ExecutorLog(action = ExecutorAction.downloadScript)    @Override    public void downloadScript() throws DownloadFailException, IOException {        for (RemoteTestcaseContent remoteTestcaseContent : this.testcaseList) {            List<ScriptInfo> scriptInfos = remoteTestcaseContent.getScripts();            for (ScriptInfo scriptInfo : scriptInfos) {                if(scriptPath.containsKey(scriptInfo.getId())){                    continue;                }                String scriptUrl = String.format("http://%s/script/%s", distestApiWeb, scriptInfo.getPath());                String localPath = Constant.localScriptPath + File.separator + scriptInfo.getMd5() + File.separator + scriptInfo.getAliasName();                downloader.start(scriptUrl, localPath);                scriptPath.put(scriptInfo.getId(), localPath);            }        }    }    @ExecutorLog(action = ExecutorAction.installApp)    public void installApp() throws InstallAppException {        super.installApp();    }    @ExecutorLog(action = ExecutorAction.launch)    public void launch() throws LaunchAppException {        super.launch();    }    @ExecutorLog(action = ExecutorAction.run)    @Override    public void run() throws TestcaseRunningException {        try {            androidApp = new DefaultAndroidApp(new File(appLocalPath));            RemoteTestcaseContent content = this.testcases.poll();            if(content == null){                log.error("没有脚本可执行");                return;            }            List<ScriptInfo> scIds = content.getScripts();            this.scripts = new ArrayBlockingQueue<>(scIds.size());            this.scripts.addAll(scIds);            this.currTestCaseId = content.getTestcaseId();            // 为了避免干扰测试流程，比如 appium 会使用到 uiautomator，关闭我们自己的 uiserver//            ui2ServerStop();            // 开始测试            startRecodPerformance();            for(;;){                if(isStop){                    break;                }                if(testcases.isEmpty() && scripts.isEmpty()){                    break;                }                if(this.scripts.isEmpty()){                    content = this.testcases.poll();                    scIds = content.getScripts();                    this.scripts = new ArrayBlockingQueue<>(scIds.size());                    this.scripts.addAll(scIds);                }                ScriptInfo runscript = this.scripts.poll();                runOneScript(runscript);                if(isComplete){                    log.info("脚本全部执行完毕");                    break;                }            }        }catch (Exception e) {            log.error("Execute script error", e);            hasError = true;            errorMsg.append(e.getMessage());        }finally {            stopRecodPerformance();        }        if(hasError) {            throw new TestcaseRunningException(errorMsg.toString());        }    }    /**     * 需要代理，必须是public方法     * @return     * @throws Exception     */    public void runOneScript(ScriptInfo runscript) throws Exception {        if(runscript == null){            isComplete = true;            return;        }        isComplete = false;        log.debug("run one script {}, deviceId {}", runscript.toString(), this.deviceId);        this.currScript = runscript;        String filePath = scriptPath.get(runscript.getId());        // 脚本替换        String url = appiumUrl.replace("0.0.0.0", "127.0.0.1");        String tempPath = this.replaceScriptByAndroid(filePath, appLocalPath, url);        log.debug("temp script path is [{}]", tempPath);        // 执行脚本        startPy(tempPath);    }    public String replaceScriptByAndroid(String localPath,                                         String appPath,                                         String appiumUrl) throws Exception {        String basePackage = appInfo.getPackageName();        String mainActivity = appInfo.getActivity();        File file = new File(localPath);        BufferedReader reader = null;        BufferedWriter bw = null;        Path transfDir = Paths.get(Constant.localScriptTmpPath, deviceId);        if(!Files.exists(transfDir)){            Files.createDirectory(transfDir);        }        File localFile = new File(localPath);        Path transfPath = Paths.get(transfDir.toString(), localFile.getName());        try {            File f = transfPath.toFile();            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));            String tempString;            int line = 1;            // 一次读入一行，直到读入null为文件结束            while ((tempString = reader.readLine()) != null) {                // 显示行号                log.debug("line [{}] : [{}]" , line, tempString);                line++;                if(tempString.contains("udid")){                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));//                    bw.write(replaceQuotationContent(tempString, this.taskCode +"", "'taskCode'"));                    bw.write(replaceQuotationContent(tempString, this.taskCode +"", "'executionTaskId'"));                    continue;                }                if(tempString.contains("deviceName")){                    bw.write(replaceQuotationContent(tempString, this.deviceId, null));                    bw.write(replaceQuotationContent(tempString, this.currScript.getId()+"", "'testSuit'"));                    bw.write(replaceQuotationContent(tempString, this.currTestCaseId+"", "'testcaseId'"));//                    bw.write(replaceQuotationContent(tempString, this.taskCode +"", "'taskCode'"));                    bw.write(replaceQuotationContent(tempString, this.taskCode +"", "'executionTaskId'"));                    continue;                }                //修改url                //self.driver = webdriver.Remote('http://localhost:4730/wd/hub', desired_caps)                if(tempString.contains("webdriver.Remote")){                    bw.write(replaceQuotationContent(tempString, appiumUrl, null));                    bw.write("\t\n");                    continue;                }                //修改app路径                // desired_caps['app'] = PATH('apps/ContactManager/ContactManager.apk')                if(tempString.contains("desired_caps['app']")){                    if(appPath.contains("\\")){                        appPath = appPath.replaceAll("\\\\", "/");                    }                    bw.write(replaceQuotationContent(tempString, appPath, null));                    bw.write("\t\n");                    continue;                }                // 取消 input manager 的安装                if(tempString.contains("desired_caps['unicodeKeyboard']")){                    continue;                }                if(tempString.contains("desired_caps['resetKeyboard']")){                    continue;                }                if(appPath.endsWith(".apk")){                    if(tempString.contains("desired_caps['appPackage']")){                        bw.write(replaceQuotationContent(tempString, basePackage, null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['appActivity']")){                        bw.write(replaceQuotationContent(tempString, mainActivity, null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['platformName']")){                        bw.write(replaceQuotationContent(tempString, "Android", null));                        bw.write("\t\n");                        continue;                    }                    if(tempString.contains("desired_caps['platformVersion']")){                        AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(deviceId);                        String version = ad.runAdbCommand("shell getprop ro.build.version.release");                        bw.write(replaceQuotationContent(tempString, version, null));                        bw.write("\t\n");                        continue;                    }                }else{                    if(tempString.contains("desired_caps['platformName']")){                        bw.write(replaceQuotationContent(tempString, "iOS", null));                        bw.write("\t\n");                        continue;                    }                }                bw.write(tempString + "\t\n");            }        } catch (IOException e) {            log.error("Modify script error, localPath: [{}], transfPath: [{}]", localPath, transfPath , e);        } finally {            if (reader != null) {                try {                    reader.close();                } catch (IOException e1) {                    log.error("Close BufferedReader in method replaceScriptByAndroid ", e1);                }            }            if(bw != null){                bw.close();            }        }        return transfPath.toString();    }    private String replaceQuotationContent(String srcStr, String replaceStr, String key){        String[] srcStrs = srcStr.split("=");        if(srcStrs.length >= 2){            String needReplaceStr = srcStrs[srcStrs.length-1];            String needReplaceKey = srcStrs[0];            String rule = "\"[^\"]*\"|'[^']*'";            Pattern p = Pattern.compile(rule);            Matcher mKey = p.matcher(needReplaceKey);            if(StringUtils.isNotEmpty(key)){                Matcher k = p.matcher(key);                if(!k.find()){                    key = "'" + key + "'";                }                if(mKey.find()){                    srcStrs[0] = mKey.replaceFirst(key);                }            }            Matcher m = p.matcher(needReplaceStr);            if(m.find()){                String newStr = m.replaceFirst("'" + replaceStr + "'").trim();                StringBuffer sb = new StringBuffer();                for(int i=0;i<srcStrs.length - 1;i++){                    sb.append(srcStrs[i]).append("=");                }                sb.append(newStr);                sb.append("\t\n");                return sb.toString();            }        }        return srcStr;    }    public void startPy(String pyPath) {        // 检查ui2是否启动，如果挂掉，则进行重启        AppiumUi2ServerDaemon ui2ServerDaemon = new AppiumUi2ServerDaemon(deviceId);        ui2Thread = new Thread(ui2ServerDaemon);        ui2Thread.start();        CommandLine commandLine = new CommandLine("python");        commandLine.addArgument(pyPath);        pyexecs = new UTF8CommonExecs(commandLine);        // 设置最大执行时间，5分钟        pyexecs.setTimeout(5*60*1000L);        try {            pyexecs.exec();            String output = pyexecs.getOutput();        } catch (IOException e) {            String error = pyexecs.getError();            log.error("Python 脚本执行错误 \n {}", error, e);            hasError = true;            errorMsg.append(error).append("\n");        }finally {            this.ui2Thread.interrupt();            ui2ServerDaemon.close();        }    }    @ExecutorLog(action = ExecutorAction.uninstallApp)    @Override    public void uninstallApp() throws UninstallAppException {        super.uninstallApp();    }    @ExecutorLog(action = ExecutorAction.stop)    @Override    public void stop() {        if(this.pyexecs != null){            this.pyexecs.destroy();        }        if(this.testcases != null){            this.testcases.clear();        }        if(this.scripts != null){            this.scripts.clear();        }        ui2ServerStop();        loggerStop();        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(taskCode)                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.stop)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }    @ExecutorLog(action = ExecutorAction.complete)    public void complete() {        super.complete();    }}