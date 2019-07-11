package com.testwa.distest.client.web.startup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.executor.task.TaskDispatcher;
import com.testwa.distest.client.component.port.*;
import com.testwa.distest.client.config.PortConfig;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.support.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wen on 16/8/27.
 */
@Slf4j
@Order(value=1)
@Component
public class EnvCheck implements CommandLineRunner {

    @Value("${distest.api.web}")
    private String apiHost;
    @Value("${username}")
    private String username;
    @Value("${password}")
    private String password;
    @Value("${distest.api.name}")
    private String applicationName;
    @Autowired
    private Environment env;

    @Override
    public void run(String... strings) throws Exception {
        Config.setEnv(env);
        AppiumPortProvider.init(PortConfig.appiumPortStart, PortConfig.appiumPortEnd);
        TcpIpPortProvider.init(PortConfig.tcpipStart, PortConfig.tcpipEnd);
        SocatPortProvider.init(PortConfig.socatStart, PortConfig.socatEnd);

        AndroidHelper.getInstance();

        checkSupportEnv();
        checkAuth(username, password);
        checkTempDirPath();
        TaskDispatcher.getInstance();
    }

    /**
     *@Description: 检查脚本执行环境是否存在
                    distest.agent.resources=/Users/wen/IdeaProjects/distest/distest-client/bin/resources
                    node.excute.path=/Users/wen/.nvm/versions/node/v8.1.4/bin/node
                    appium.js.path=/Users/wen/dev/testWa/appium165beta/build/lib/main.js
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/25
     */
    private void checkSupportEnv() {
        List<String> keys = Arrays.asList("distest.agent.resources", "node.excute.path", "appium.js.path");
        keys.forEach( k -> {
            String value = env.getProperty(k);
            if(StringUtils.isBlank(value)){
                log.error("{} 的值为空，请配置好启动参数", k);
                System.exit(0);
            }else{
                Path p = Paths.get(value);
                if(!Files.exists(p)){
                    log.error("路径 {} 不存在，请检查启动参数", p.toString());
                    System.exit(0);
                }
                log.debug("{}: {}", k, p.toString());
            }
        });

        String androidHome = System.getenv("ANDROID_HOME");
        if(StringUtils.isBlank(androidHome)){
            log.error("Android 环境变量找不到，请检查 ANDROID_HOME");
            System.exit(0);
        }else{
            Path p = Paths.get(androidHome);
            if(!Files.exists(p)){
                log.error("目录 {} 不存在，请检查启动参数", p.toString());
                System.exit(0);
            }
        }

    }

    private void checkAuth(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            System.exit(0);
        }
        String uri = StringUtils.isBlank(applicationName) ? apiHost : apiHost + "/" + applicationName;
        String loginUrl = String.format("http://%s/v1/auth/login", uri);
        User loginUser = new User(username, password);

        String content = OkHttpUtil.postJsonParams(loginUrl, JSON.toJSONString(loginUser));
        Object result = JSONObject.parse(content);
        Integer resultCode = ((JSONObject) result).getInteger("code");

        if (resultCode == 0) {
            JSONObject data = (JSONObject) ((JSONObject) result).get("data");
            UserInfo.token = data.getString("accessToken");
            UserInfo.username = username;
            log.info("登录成功，username: {}, token: {}", UserInfo.username, UserInfo.token);
        } else {
            log.error("登录{}失败，返回{}", loginUrl, content);
            System.exit(0);
        }
    }

    private class User {
        public String username;
        public String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private void checkTempDirPath() {
        File localAppDir = new File(Constant.localAppPath);
        if (!localAppDir.exists()) {
            localAppDir.mkdirs();
        }

        File localScriptDir = new File(Constant.localScriptPath);
        if (!localScriptDir.exists()) {
            localScriptDir.mkdirs();
        }

        File localScriptTmpDir = new File(Constant.localScriptTmpPath);
        if (!localScriptTmpDir.exists()) {
            localScriptTmpDir.mkdirs();
        }

        File localAppiumlogDir = new File(Constant.localAppiumLogPath);
        if (!localAppiumlogDir.exists()) {
            localAppiumlogDir.mkdirs();
        }

        File localScreenshotDir = new File(Constant.localScreenshotPath);
        if (!localScreenshotDir.exists()) {
            localScreenshotDir.mkdirs();
        }

        File locallogcatDir = new File(Constant.localLogcatPath);
        if (!locallogcatDir.exists()) {
            locallogcatDir.mkdirs();
        }
        File localVideoDir = new File(Constant.localVideoPath);
        if (!localVideoDir.exists()) {
            localVideoDir.mkdirs();
        }
        File localCrawlerOutDir = new File(Constant.localCrawlerOutPath);
        if (!localCrawlerOutDir.exists()) {
            localCrawlerOutDir.mkdirs();
        }
        File localActionScreenOutDir = new File(Constant.localActionScreenPath);
        if (!localActionScreenOutDir.exists()) {
            localActionScreenOutDir.mkdirs();
        }

        log.info("App: {}", Constant.localAppPath);
        log.info("Logcat: {}", Constant.localLogcatPath);
        log.info("Screen: {}", Constant.localScreenshotPath);
        log.info("Script: {}", Constant.localScriptPath);
        log.info("Video: {}", Constant.localVideoPath);
        log.info("Crawler: {}", Constant.localCrawlerOutPath);
    }

}
