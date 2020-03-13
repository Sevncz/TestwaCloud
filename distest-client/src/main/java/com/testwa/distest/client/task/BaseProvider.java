package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.testwa.core.script.vo.TaskEnvVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 处理基本逻辑，上传，下载，和脚本语言无关
 */
@Slf4j
@Component
public class BaseProvider {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AsyncProvider asyncProvider;
    @Value("${distest.api.web}")
    private String apiUrl;
    @Value("${application.version}")
    private String applicationVersion;
    @Value("${download.url}")
    private String downloadUrl;

    public void uploadResult(TaskVO msg, String resultPath, int success) throws IOException {

        if (Files.list(Paths.get(resultPath)).count() > 0) {
            List<CompletableFuture<ResponseEntity<FileUploadEntity>>> responseFutures = Files.list(Paths.get(resultPath)).map(f -> asyncProvider.asyncUploadTaskResult(f, msg, resultPath)).collect(Collectors.toList());
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[responseFutures.size()]));
            CompletableFuture<List<ResponseEntity<FileUploadEntity>>> allResponseFuture = allFutures.thenApply(v -> {
                return responseFutures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList());
            });
            CompletableFuture<Long> countFuture = allResponseFuture.thenApply(responses -> {
                return responses.stream()
                        .filter(responseEntity -> responseEntity.getBody().getCode() == 0)
                        .count();
            });
            try {
                log.info("保存结果文件数量：{}", countFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        // 上传完成，通知任务已完成
        TaskEnvVO envVO = new TaskEnvVO();
        AgentInfo agentInfo = AgentInfo.getAgentInfo();
        envVO.setAgentVersion(applicationVersion);
        envVO.setJavaVersion(agentInfo.getJavaVersion());
        envVO.setOsVersion(agentInfo.getOsVersion());
        envVO.setNodeVersion("1.13");
        envVO.setPythonVersion("3.7");
        envVO.setDeviceId(msg.getDeviceId());

        String url = "http://" + apiUrl + "/v2/task/" + msg.getTaskCode() + "/finish/" + success;
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("X-TOKEN", UserInfo.token);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> formEntity = new HttpEntity<>(JSON.toJSONString(envVO), requestHeaders);
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, formEntity, String.class);
        log.info("生成报告返回：{}", responseEntity.getBody());
    }


    public String downloadApp(String appPath) {
        String appUrl = String.format("http://%s/app/%s", downloadUrl, appPath);
        String appLocalPath = Constant.localAppPath + File.separator + appPath;

        // 检查是否有和该app md5一致的
        try {
            log.info("应用路径：{}", appLocalPath);
            if (Files.notExists(Paths.get(appLocalPath))) {
                Downloader d = new Downloader();
                d.start(appUrl, appLocalPath);
            }
        } catch (DownloadFailException | IOException e) {
            e.printStackTrace();
        }
        return appLocalPath;
    }

    public ResponseEntity<FileUploadEntity> uploadFile(Path f) {
        String url = "http://" + apiUrl + "/v1/fileSupport/single";
        HttpHeaders requestHeadersFile = new HttpHeaders();
        requestHeadersFile.set("X-TOKEN", UserInfo.token);
        FileSystemResource resource = new FileSystemResource(f.toFile());
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, requestHeadersFile);
        ResponseEntity<FileUploadEntity> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, FileUploadEntity.class);
        log.info("上传返回：{}", responseEntity.getBody().getData());
        return responseEntity;
    }


}
