package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.testwa.core.script.vo.TaskResultVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.distest.client.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class AsyncProvider {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${distest.api.web}")
    private String apiUrl;

    @Async("taskExecutor")
    public CompletableFuture<ResponseEntity<FileUploadEntity>> asyncUploadTaskResult(Path f, TaskVO msg, String resultPath) {
        String url = "http://" + apiUrl + "/v1/fileSupport/single";
        HttpHeaders requestHeadersFile = new HttpHeaders();
        requestHeadersFile.set("X-TOKEN", UserInfo.token);
        FileSystemResource resource = new FileSystemResource(f.toFile());
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, requestHeadersFile);
        ResponseEntity<FileUploadEntity> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, FileUploadEntity.class);
        log.debug("上传返回：{}", responseEntity.getBody().getData());

        if (responseEntity.getStatusCode().value() == 200 && responseEntity.getBody().getCode() == 0) {
            // 生成TaskResult对象
            TaskResultVO resultVO = new TaskResultVO();
            resultVO.setResult(f.getFileName().toString().replace(resultPath, ""));
            resultVO.setTaskCode(msg.getTaskCode());
            resultVO.setUrl(responseEntity.getBody().getData());
            resultVO.setDeviceId(msg.getDeviceId());
            String url2 = "http://" + apiUrl + "/v2/task/result";
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("X-TOKEN", UserInfo.token);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> formEntity = new HttpEntity<>(JSON.toJSONString(resultVO), requestHeaders);
            ResponseEntity<String> responseEntity1 = this.restTemplate.postForEntity(url2, formEntity, String.class);
            log.debug("保存TaskResult返回：{}", responseEntity1.getBody());
        }
        return CompletableFuture.completedFuture(responseEntity);
    }

}
