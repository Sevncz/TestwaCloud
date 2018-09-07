package com.testwa.distest.client.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.GrpcClientService;
import io.rpc.testwa.task.StepRequest;
import io.rpc.testwa.task.StepRequestOrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Controller
public class IndexController {
    @Autowired
    private GrpcClientService grpcClientService;

    @RequestMapping("/")
    String index() {
        return "Hello World!";
    }

    @RequestMapping({ "/client" })
    @ResponseBody
    public String action(HttpServletRequest request) {
        String urlInfo = parseInputStreamFormUrlToJson(request);
        if(StringUtils.isBlank(urlInfo) || !urlInfo.contains("executionTaskId")) {
            log.warn("appium 。。。。。。。。。。");
            return "error";
        }
        grpcClientService.procedureInfoUpload(urlInfo);
        JSONObject appiumStepJson = JSON.parseObject(urlInfo);
        String taskId = appiumStepJson.getString("executionTaskId");
        String scriptId = appiumStepJson.getString("testSuit");
        String testcaseId = appiumStepJson.getString("testcaseId");
        String sessionId = appiumStepJson.getString("sessionId");
        if(StringUtils.isNotBlank(taskId) && StringUtils.isNotBlank(scriptId) && StringUtils.isNotBlank(testcaseId) && StringUtils.isNotBlank(sessionId)){
            // {"getSource":0,"value":null,"runtime":468,"cpurate":"22","memory":"100012","battery":null,
            // "sessionId":"ace91834-73c7-4fc0-8a85-004caec5154d","deviceId":"b15d91f","testSuit":"3",
            // "testcaseId":"3","taskCode":"185","screenshotPath":"1527502356422.png",
            // "description":"No Driver found for this session, probably appium error, please restart appium!",
            // "command":{"action":"等待","params":"60000ms"}}
            String value = appiumStepJson.getString("value");
            if(StringUtils.isBlank(value)){
                value = "null";
            }
            Integer status = appiumStepJson.getInteger("status");
            Long runtime = appiumStepJson.getLong("runtime");
            JSONObject command = appiumStepJson.getJSONObject("command");
            String action = command.getString("action");
            String params = command.getString("params");

            StepRequest.StepStatus stepStatus = StepRequest.StepStatus.forNumber(status);
            if(stepStatus == null) {
                stepStatus = StepRequest.StepStatus.FAIL;
            }
            log.info(urlInfo);
            StepRequest.Builder builder = StepRequest.newBuilder();
            builder.setToken(UserInfo.token)
                    .setTaskCode(Long.parseLong(taskId))
                    .setDeviceId(appiumStepJson.getString("deviceId"))
                    .setAction(StepRequest.StepAction.operation)
                    .setStatus(stepStatus)
                    .setRuntime(runtime)
                    .setValue(value)
                    .setTimestamp(System.currentTimeMillis())
                    .setTestcaseId(Long.parseLong(testcaseId))
                    .setScriptId(Long.parseLong(scriptId))
                    .setSessionId(sessionId);

            String screenPath = appiumStepJson.getString("screenshotPath");
            if(StringUtils.isNotBlank(screenPath)) {
                builder.setImg(screenPath);
            }
            if(StringUtils.isNotBlank(action)) {
                builder.setCommadAction(action);
            }
            if(StringUtils.isNotBlank(params)) {
                builder.setCommadParams(params);
            }
            grpcClientService.saveStep(builder.build());
        }

        return "ok";
    }

    @RequestMapping({ "/client/{deviceId}/{testcaselogId}/{prot}" })
    @ResponseBody
    public String start(@PathVariable("deviceId")String deviceId, @PathVariable("testcaselogId")Integer testcaselogId, @PathVariable("prot")Integer prot, HttpServletRequest request){
        log.info("runOneScript schedule py");
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
