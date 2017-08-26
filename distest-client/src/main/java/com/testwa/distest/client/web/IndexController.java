package com.testwa.distest.client.web;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.task.Testcase;
import com.testwa.distest.client.task.TestcaseTaskCaches;
import com.testwa.distest.client.util.Http;
import io.rpc.testwa.task.ProcedureInfoRequest;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.RemoteLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by wen on 7/30/16.
 */
@Controller
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private Environment evn;

    @Autowired
    private HttpService httpService;

    @RequestMapping("/")
    String index() {
        return "Hello World!";
    }

    @RequestMapping({ "/client" })
    @ResponseBody
    public String action(HttpServletRequest request) {

        String urlInfo = parseInputStreamFormUrlToJson(request);
        Map<String, Object> payload = JSON.parseObject(urlInfo);
        logger.info("Receive message, [{}]", payload);
        String sessionId = "";
        if(payload.containsKey("sessionId")){
            sessionId = payload.get("sessionId")  == null ? "": (String)payload.getOrDefault("sessionId", "");
        }

        String deviceId = (String)payload.getOrDefault("deviceId", "");
        String logcatFileName = "";
        String action = "";
        String params = "";
        if(payload.containsKey("command")){
            if(payload.get("command") instanceof String){
                action = (String) payload.get("command");
            }
            if(payload.get("command") instanceof Map){
                Map cmd = (Map)payload.get("command");
                if(cmd != null){
                    action = cmd.get("action") == null ?"" : (String)cmd.getOrDefault("action", "");
                    params = cmd.get("beans") == null ?"" : (String)cmd.getOrDefault("beans", "");
                }
            }

        }

        String screenpath = (String) payload.getOrDefault("screenshotPath", "");

        ProcedureInfoRequest fb = null;
        fb = ProcedureInfoRequest.newBuilder()
                .setStatus((Integer) payload.getOrDefault("status", "0"))
                .setValue(payload.getOrDefault("value", "") + "")
                .setRuntime((Integer) payload.getOrDefault("runtime", 0))
                .setCpurate(payload.getOrDefault("cpurate", "0")+"")
                .setMemory(payload.getOrDefault("memory", 0)+"")
                .setSessionId( sessionId )
                .setDeviceId( deviceId )
                .setScreenshotPath( screenpath )
                .setActionBytes(ByteString.copyFromUtf8(action))
                .setParams(params)
                .setTimestamp(TimeUtil.getTimestampLong())
                .setLogcatFile(logcatFileName)
                .setDescription("")
                .setUserId(UserInfo.token)
                .setExecutionTaskId((String)payload.getOrDefault("executionTaskId", "") )
                .setTestcaseId((String)payload.getOrDefault("testcaseId", "") )
                .setScriptId((String)payload.getOrDefault("testSuit", "") )
                .build();
        MainSocket.getSocket().emit(WebsocketEvent.FB_RUNNGING_LOG, fb.toByteArray());
        return "ok";
    }

    @RequestMapping({ "/client/{deviceId}/{testcaselogId}/{prot}" })
    @ResponseBody
    public String start(@PathVariable("deviceId")String deviceId, @PathVariable("testcaselogId")Integer testcaselogId, @PathVariable("prot")Integer prot, HttpServletRequest request){
        logger.info("start schedule py");
        return "ok";
    }


    public String parseInputStreamFormUrlToJson(ServletRequest request) {
        StringBuffer urlInfo = new StringBuffer();

        InputStream in = null;
        try {
            in = request.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);

            byte[] buffer = new byte[1024];
            int iRead;
            while ((iRead = buf.read(buffer)) != -1) {
                urlInfo.append(new String(buffer, 0, iRead, "UTF-8"));
            }
        } catch (Exception e) {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return urlInfo.toString();
    }

}
