package com.testwa.distest.client.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.support.OkHttpUtil;
import com.testwa.distest.client.web.startup.EnvCheck;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoginService {

    @Value("${username}")
    private String username;
    @Value("${password}")
    private String password;
    @Value("${distest.api.name}")
    private String applicationName;
    @Value("${distest.api.web}")
    private String apiHost;


    public boolean login() {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            log.error("登录信息为空");
            return false;
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
            return false;
        }
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

}
