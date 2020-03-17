package com.testwa.distest.client.task;

import com.testwa.core.script.Function;
import com.testwa.core.script.util.FileUtil;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        String pyPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + ".py";
        String resultPath = Constant.localScriptPath + File.separator + msg.getTaskCode() + "result";
        log.info("python 脚本路径 {} ", pyPath);
        try {
            if (!Files.exists(Paths.get(pyPath))) {
                Files.createFile(Paths.get(pyPath));
            }
            FileUtil.ensureExistEmptyDir(resultPath);
            Files.write(Paths.get(pyPath), scriptContent.getBytes());
            CommandLine commandLine = new CommandLine("pytest");
            commandLine.addArgument(pyPath);
            commandLine.addArgument("--alluredir");
            commandLine.addArgument(resultPath);
            commandLine.addArgument("--reruns");
            commandLine.addArgument("5");
            UTF8CommonExecs pyexecs = new UTF8CommonExecs(commandLine);
            // 设置最大执行时间，30分钟
            pyexecs.setTimeout(30 * 60 * 1000L);
            int success = 0;
            try {
                pyexecs.exec();
            } catch (IOException e) {
                success = success + 1;
                String output = pyexecs.getOutput();
                log.error(output);
                log.error("py 执行失败", e);
            } finally {
                // 上传result json
                uploadResult(msg, resultPath, success);
            }
        } catch (IOException e) {
            log.error("py 写入失败", e);
        }
    }


    public List<List<Function>> generatorFunctions(TaskVO msg) throws Exception {
        return msg.getScriptCases().stream().map(scriptCaseVO -> {
            try {
                return generatorFunctions(scriptCaseVO.getScriptCaseId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public List<Function> generatorFunctions(String scriptCaseId) throws Exception {
        String url = "http://" + apiUrl + "/v2/script/" + scriptCaseId + "/pyActionCode";
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("X-TOKEN", UserInfo.token);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> request = new HttpEntity<>(requestHeaders);
        ResponseEntity<FunctionCodeEntity> responseEntity = this.restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                FunctionCodeEntity.class
        );
        if (responseEntity.getStatusCode().value() == 200 && responseEntity.getBody().getCode() == 0) {
            return responseEntity.getBody().getData();
        }
        throw new Exception("代码生成异常");
    }

}
