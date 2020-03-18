package com.testwa.distest.client.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.component.executor.uiautomator2.Ui2Command;
import com.testwa.distest.client.component.executor.uiautomator2.Ui2Server;
import com.testwa.distest.client.component.executor.worker.FunctionalPythonExecutor;
import com.testwa.distest.client.config.CacheProperty;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.GrpcClientService;
import com.testwa.distest.client.task.BaseProvider;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wen on 7/30/16.
 */
@Slf4j
@RestController
public class IndexController {
    @Autowired
    private BaseProvider baseProvider;

    @GetMapping("/")
    public String index() {
        return "hello";
    }

    @PostMapping("/case/start")
    public void caseStart(@RequestBody CaseStartVO vo) throws InterruptedException {

        // 启动ui2
//        Ui2Server ui2Server = new Ui2Server(vo.getDeviceId());
//        ui2Server.start();
        Ui2Command ui2Command = new Ui2Command(vo.getSystemPort());
        ui2Command.startInstallCheck(vo.getDeviceId());
        // 手动安装app
        baseProvider.installApp(vo.getDeviceId(), vo.getAppPath());
        ui2Command.stopInstallCheck();

        ui2Command.startProcessRunningAlter();
        log.info("[{}]安装app {}", vo.getDeviceId(), vo.getAppPath());
        // 手动启动app
        ADBCommandUtils.launcherApp(vo.getDeviceId(), vo.getAppPath());
        TimeUnit.SECONDS.sleep(5);
        ui2Command.stopProcessRunningAlter();
        log.info("[{}]启动app {}", vo.getDeviceId(), vo.getAppPath());
//        ui2Server.close();
    }

}
