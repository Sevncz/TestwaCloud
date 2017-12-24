package com.testwa.distest.client.web;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.Clients;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.client.grpc.GClient;
import com.testwa.distest.client.control.client.grpc.pool.GClientPool;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.task.Testcase;
import com.testwa.distest.client.task.TestcaseTaskCaches;
import com.testwa.distest.client.util.Http;
import io.rpc.testwa.task.ProcedureInfoRequest;
import io.rpc.testwa.task.ProcedureInfoUploadRequest;
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
    @Autowired
    private GClientPool gClientPool;

    @RequestMapping("/")
    String index() {
        return "Hello World!";
    }

    @RequestMapping({ "/client" })
    @ResponseBody
    public String action(HttpServletRequest request) {

        String urlInfo = parseInputStreamFormUrlToJson(request);
        Map<String, Object> payload = JSON.parseObject(urlInfo);
        payload.put("timestamp", TimeUtil.getTimestampLong());
        payload.put("createDate", TimeUtil.getTimestamp());
        urlInfo = JSON.toJSONString(payload);
        ProcedureInfoUploadRequest procedureInfoUploadRequest = ProcedureInfoUploadRequest.newBuilder()
                                                                    .setInfoJson(urlInfo)
                                                                    .build();

        GClient c = gClientPool.getClient();
        c.taskService().procedureInfoUpload(procedureInfoUploadRequest);
        gClientPool.release(c);

//        logger.info("Receive message, [{}]", payload);
//        String sessionId = "";
//        if(payload.containsKey("sessionId")){
//            sessionId = payload.get("sessionId")  == null ? "": (String)payload.getOrDefault("sessionId", "");
//        }
//
//        String deviceId = (String)payload.getOrDefault("deviceId", "");
//        String logcatFileName = "";
//        String action = "";
//        String params = "";
//        if(payload.containsKey("command")){
//            if(payload.get("command") instanceof String){
//                action = (String) payload.get("command");
//            }
//            if(payload.get("command") instanceof Map){
//                Map cmd = (Map)payload.get("command");
//                if(cmd != null){
//                    action = cmd.get("action") == null ?"" : (String)cmd.getOrDefault("action", "");
//                    params = cmd.get("beans") == null ?"" : (String)cmd.getOrDefault("beans", "");
//                }
//            }
//
//        }
//
//        String screenpath = (String) payload.getOrDefault("screenshotPath", "");
//
//        Integer status = getInteger(payload, "status");
//        Integer runtime = getInteger(payload, "runtime");
//        Integer cpurate = getInteger(payload, "cpurate");
//        Integer memory = getInteger(payload, "memory");
//        Long taskId = 0l;
//        if(StringUtils.isNotBlank((String) payload.getOrDefault("executionTaskId", ""))){
//            taskId = Long.parseLong((String) payload.getOrDefault("executionTaskId", 0));
//        }
//        Long testcaseId = 0l;
//        if(StringUtils.isNotBlank((String) payload.getOrDefault("testcaseId", ""))){
//            testcaseId = Long.parseLong((String) payload.getOrDefault("testcaseId", 0));
//        }
//        Long testSuit = 0l;
//        if(StringUtils.isNotBlank((String) payload.getOrDefault("testSuit", ""))){
//            testSuit = Long.parseLong((String) payload.getOrDefault("testSuit", 0));
//        }
//
//        ProcedureInfoRequest fb = null;
//        fb = ProcedureInfoRequest.newBuilder()
//                .setStatus( status )
//                .setValue(payload.getOrDefault("value", "") + "")
//                .setRuntime(runtime)
//                .setCpurate( cpurate )
//                .setMemory( memory )
//                .setSessionId( sessionId )
//                .setDeviceId( deviceId )
//                .setScreenshotPath( screenpath )
//                .setActionBytes(ByteString.copyFromUtf8(action))
//                .setParams(params)
//                .setTimestamp(TimeUtil.getTimestampLong())
//                .setLogcatFile(logcatFileName)
//                .setDescription("")
//                .setToken(UserInfo.token)
//                .setTaskId( taskId )
//                .setTestcaseId( testcaseId )
//                .setScriptId( testSuit )
//                .build();
//        MainSocket.getSocket().emit(WebsocketEvent.FB_RUNNGING_LOG, fb.toByteArray());
        return "ok";
    }

    private Integer getInteger(Map<String, Object> payload, String key) {
        Integer runtime = 0;
        if(payload.getOrDefault(key, 0) instanceof String){
            String runtime_str = (String) payload.getOrDefault(key, 0);
            if(StringUtils.isNotEmpty(runtime_str)){
                runtime = Integer.parseInt(runtime_str);
            }
        }else if(payload.getOrDefault(key, 0) instanceof Integer){
            runtime = (Integer) payload.getOrDefault(key, 0);
        }
        return runtime;
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
