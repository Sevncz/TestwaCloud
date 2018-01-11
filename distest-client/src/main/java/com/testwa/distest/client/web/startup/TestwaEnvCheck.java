package com.testwa.distest.client.web.startup;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.android.DeviceManager;
import com.testwa.distest.client.appium.utils.Config;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.boost.MessageCallback;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.GrpcClientService;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.util.Constant;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by wen on 16/8/27.
 */
@Log4j2
@Component
public class TestwaEnvCheck implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Autowired
    private HttpService httpService;
    @Autowired
    private GrpcClientService gClientService;
    @Autowired
    @Qualifier("startRemoteClientCallbackImpl")
    private MessageCallback startRemoteClientCB;
    @Autowired
    @Qualifier("startTestcaseCallbackImpl")
    private MessageCallback startTestcaseClientCB;

    @Override
    public void run(String... strings) throws Exception {
        checkAuth(env.getProperty("username"), env.getProperty("password"));
        checkTempDirPath();
        Config.setEnv(env);
        startDeviceManager();

    }

    private boolean checkAuth(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }
        String agentWebUrl = env.getProperty("agent.web.url");
        FutureCallback cb = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                int code = response.getStatusLine().getStatusCode();
                if (code != 200) {
                    log.error("Request code not 200， code is {}", code);
                    System.exit(0);
                } else {
                    try {
                        String content = EntityUtils.toString(response.getEntity());
                        Object result = JSONObject.parse(content);
                        Integer resultCode = ((JSONObject) result).getInteger("code");

                        if (resultCode == 0) {
                            JSONObject data = (JSONObject) ((JSONObject) result).get("data");
                            String token = data.getString("accessToken");
                            UserInfo.token = token;

                            String url = env.getProperty("agent.socket.url");
                            MainSocket.connect(url, token);
                            MainSocket.receive(WebsocketEvent.ON_START, startRemoteClientCB);
                            MainSocket.receive(WebsocketEvent.ON_TESTCASE_RUN, startTestcaseClientCB);

                        } else {
                            log.error("login error {}", resultCode);
                            System.exit(0);
                        }

                    } catch (IOException e) {
                        log.error("Remote server fail", e);
                        System.exit(0);
                    } catch (JSONException e) {
                        // 公司代理环境下 返回html页面
                        log.error("Remote server response not parsable!", e);
                        System.exit(0);
                    }
                }
            }

            @Override
            public void failed(Exception ex) {
                log.error("Request failed", ex);
                System.exit(0);
            }

            @Override
            public void cancelled() {
                log.error("Request cancelled");
                System.exit(0);

            }
        };
        httpService.postJson(String.format("%s/api/auth/login", agentWebUrl), new User(username, password), cb);
        return true;
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

        log.info("Constant.localAppPath --> {}", Constant.localAppPath);
        log.info("Constant.locallogcatPath --> {}", Constant.localLogcatPath);
        log.info("Constant.localScreenshotPath --> {}", Constant.localScreenshotPath);
        log.info("Constant.localScriptPath --> {}", Constant.localScriptPath);
    }

    private void startDeviceManager() {
        new Thread(() -> DeviceManager.getInstance().start()).start();
    }

}
