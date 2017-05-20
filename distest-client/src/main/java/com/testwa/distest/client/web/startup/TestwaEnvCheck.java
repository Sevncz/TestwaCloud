package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.appium.utils.Config;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by wen on 16/8/27.
 */
@Component
public class TestwaEnvCheck implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(TestwaEnvCheck.class);

    @Autowired
    private Environment evn;

    @Autowired
    private HttpService httpService;

    @Override
    public void run(String... strings) throws Exception {
        boolean isAuth = checkAuth(evn.getProperty("username"), evn.getProperty("password"));
        if(!isAuth){
            LOG.error("username or password not match");
            System.exit(0);
        }
        checkTempDirPath();
        Config.setEnv(evn);
    }

    private boolean checkAuth(String username, String password) {
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return false;
        }
        String agentWebUrl = evn.getProperty("agent.web.url");
        FutureCallback cb = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                int code = response.getStatusLine().getStatusCode();
                if(code != 200){
                    LOG.error("Request code not 200， code is {}", code);
                    System.exit(0);
                }else{
                    try {
                        String json_string = EntityUtils.toString(response.getEntity());
                        JSONObject result = new JSONObject(json_string);
                        Integer resultCode = result.getInt("code");

                        if(resultCode == 0){
                            JSONObject data = (JSONObject) result.get("data");
                            String token = data.getString("access_token");
                            String userId = data.getString("userId");

                            UserInfo.token = token;
                            UserInfo.userId = userId;

                        }else{
                            LOG.error("Login error {}", resultCode);
                            System.exit(0);
                        }

                    } catch (IOException e) {
                        LOG.error("Remote server fail", e);
                        System.exit(0);
                    }
                }
            }

            @Override
            public void failed(Exception ex) {
                LOG.error("Request failed", ex);
                System.exit(0);
            }

            @Override
            public void cancelled() {
                LOG.error("Request cancelled");
                System.exit(0);

            }
        };
        httpService.postJson(String.format("%s/account/purelogin", agentWebUrl), new User(username, password), cb);
//        User userData = new User(username, password)
//        Http.post(String.format("%s/account/purelogin", agentWebUrl), 60000, )
        return true;
    }

    private class User{
        public String username;
        public String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private void checkTempDirPath(){
        File localAppDir = new File(Constant.localAppPath);
        if(!localAppDir.exists()){
            localAppDir.mkdirs();
        }

        File localScriptDir = new File(Constant.localScriptPath);
        if(!localScriptDir.exists()){
            localScriptDir.mkdirs();
        }

        File localScriptTmpDir = new File(Constant.localScriptTmpPath);
        if(!localScriptTmpDir.exists()){
            localScriptTmpDir.mkdirs();
        }

        File localAppiumLogDir = new File(Constant.localAppiumLogPath);
        if(!localAppiumLogDir.exists()){
            localAppiumLogDir.mkdirs();
        }

        File localScreenshotDir = new File(Constant.localScreenshotPath);
        if(!localScreenshotDir.exists()){
            localScreenshotDir.mkdirs();
        }

        File localLogcatDir = new File(Constant.localLogcatPath);
        if(!localLogcatDir.exists()){
            localLogcatDir.mkdirs();
        }

        LOG.info("Constant.localAppPath --> {}", Constant.localAppPath);
        LOG.info("Constant.localLogcatPath --> {}", Constant.localLogcatPath);
        LOG.info("Constant.localScreenshotPath --> {}", Constant.localScreenshotPath);
        LOG.info("Constant.localScriptPath --> {}", Constant.localScriptPath);
    }

}
