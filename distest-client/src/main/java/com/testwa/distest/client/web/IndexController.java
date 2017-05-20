package com.testwa.distest.client.web;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.testwa.distest.client.appium.utils.Config;
import com.testwa.distest.client.boost.TestwaSocket;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.HelloGrpcService;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.task.Testcase;
import com.testwa.distest.client.task.TestcaseTaskCaches;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.client.util.Http;
import com.testwa.distest.client.util.Identities;
import com.testwa.distest.client.util.Im4JavaUtils;
import com.testwa.distest.client.util.TimeUtil;
import io.grpc.testwa.testcase.RunningLogRequest;
import io.socket.client.Socket;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by wen on 7/30/16.
 */
@RestController
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final Socket socket;

    @Autowired
    private Environment evn;

    @Autowired
    private HttpService httpService;

    @Autowired
    private HelloGrpcService helloGrpcService;

    @Autowired
    public IndexController(Socket socket) {
        this.socket = socket;
    }

    @RequestMapping("/")
    String index() {
        helloGrpcService.sayHello();
        return "Hello World!";
    }

    @RequestMapping({ "/client" })
    @ResponseBody
    public String action(@RequestBody Map<String, Object> payload) {
        logger.info("Receive message, [{}]", payload);
        String sessionId = "";
        if(payload.containsKey("sessionId")){
            sessionId = payload.get("sessionId")  == null ? "": (String)payload.getOrDefault("sessionId", "");
        }

        String deviceId = (String)payload.getOrDefault("deviceId", "");
        String logcatFileName = "";
        if(StringUtils.isNotBlank(sessionId) && StringUtils.isNotBlank(deviceId) ){
            Testcase tc = TestcaseTaskCaches.getTCBySerial(deviceId);
            String appiumUrl = tc.getAp().appiumMan.getAppiumUrl().toString().replace("0.0.0.0", "127.0.0.1");
            logcatFileName = Http.getLogcat(String.format("%s/session/%s/log", appiumUrl, sessionId), ImmutableMap.of(RemoteLogs.TYPE_KEY, "logcat"), tc);
        }else{
            logger.error("SessionId or deviceId == null, {}, {}", sessionId, deviceId);
        }

        String action = "";
        String params = "";
        if(payload.containsKey("command")){
            Map cmd = (Map)payload.get("command");
            if(cmd != null){
                action = cmd.get("action") == null ?"" : (String)cmd.getOrDefault("action", "");
                params = cmd.get("params") == null ?"" : (String)cmd.getOrDefault("params", "");
            }
        }

        String screenpath = (String) payload.getOrDefault("screenshotPath", "");

        RunningLogRequest fb = null;
        fb = RunningLogRequest.newBuilder()
                .setStatus((Integer) payload.getOrDefault("status", "0"))
                .setValue(payload.getOrDefault("value", "") + "")
                .setRuntime((Integer) payload.getOrDefault("runtime", 0))
                .setCpurate(payload.getOrDefault("cpurate", "0")+"")
                .setMemory(payload.getOrDefault("memory", 0)+"")
                .setSessionId( sessionId )
                .setDeviceId( deviceId )
                .setReportDetailId((String)payload.getOrDefault("testcaseId", "") )
                .setScriptId((String)payload.getOrDefault("testSuit", "") )
                .setScreenshotPath( screenpath )
                .setActionBytes(ByteString.copyFromUtf8(action))
                .setParams(params)
                .setTimestamp(TimeUtil.getTimestampLong())
                .setLogcatFile(logcatFileName)
                .setDescription("")
                .setUserId(UserInfo.userId)
                .build();
        TestwaSocket.getSocket().emit("feedback.runninglog", fb.toByteArray());
        return "ok";
    }

    @RequestMapping({ "/client/{deviceId}/{testcaselogId}/{prot}" })
    @ResponseBody
    public String start(@PathVariable("deviceId")String deviceId, @PathVariable("testcaselogId")Integer testcaselogId, @PathVariable("prot")Integer prot, HttpServletRequest request){
        logger.info("start run py");
        return "ok";
    }

}
