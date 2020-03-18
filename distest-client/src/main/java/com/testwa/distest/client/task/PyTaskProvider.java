package com.testwa.distest.client.task;

import com.testwa.core.script.Function;
import com.testwa.core.script.util.FileUtil;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理具体脚本
 */
@Slf4j
@Component
public class PyTaskProvider extends BaseProvider{

    @Autowired
    private RestTemplate restTemplate;
    @Value("${distest.api.web}")
    private String apiUrl;

    public void runPyScript(TaskVO msg, String scriptContent) {
        Path pyPath = Paths.get(Constant.localScriptPath, msg.getTaskCode() + System.currentTimeMillis() + ".py");
        Path resultPath = Paths.get(Constant.localScriptPath, msg.getTaskCode() + "result");
        log.info("python 脚本路径 {} ", pyPath);
        try {
            if (!Files.exists(pyPath)) {
                Files.createFile(pyPath);
            }
            FileUtil.ensureExistEmptyDir(resultPath.toString());
            Files.write(pyPath, scriptContent.getBytes());
            CommandLine commandLine = new CommandLine("pytest");
            commandLine.addArgument(pyPath.toString());
            commandLine.addArgument("--alluredir");
            commandLine.addArgument(resultPath.toString());
            commandLine.addArgument("--reruns");
            commandLine.addArgument("5");
            UTF8CommonExecs pyexecs = new UTF8CommonExecs(commandLine);
            // 设置最大执行时间，30分钟
            pyexecs.setTimeout(30 * 60 * 1000L);
            try {
                pyexecs.exec();
            } catch (IOException e) {
                String output = pyexecs.getOutput();
                log.error(output);
                log.error("py 执行失败", e);
            } finally {
                // 上传result json
                log.info("[{}]保存执行结果", msg.getDeviceId());
                uploadResult(msg, resultPath.toString());
            }
        } catch (IOException e) {
            log.error("py 写入失败", e);
        }
    }

}
