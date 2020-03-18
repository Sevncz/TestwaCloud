package com.testwa.distest.client.task;

import com.alibaba.fastjson.JSON;
import com.android.ddmlib.IDevice;
import com.testwa.core.cmd.KeyCode;
import com.testwa.core.script.vo.TaskEnvVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.distest.client.android.ADBCommandUtils;
import com.testwa.distest.client.android.ADBTools;
import com.testwa.distest.client.android.JadbDeviceManager;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.StepResult;
import com.testwa.distest.client.component.executor.uiautomator2.Bounds;
import com.testwa.distest.client.component.executor.uiautomator2.Ui2Command;
import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriverCapabilities;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.exception.InstallAppException;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.jadb.JadbDevice;
import io.rpc.testwa.task.StepRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.testwa.distest.client.component.executor.worker.AbstractExecutor.TESTWA_PWD;

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
    @Value("${download.url}")
    private String downloadUrl;

    public void uploadResult(TaskVO msg, String resultPath) throws IOException {

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
    }


    public String downloadApp(String appPath) {
        String appUrl = String.format("http://%s/app/%s", downloadUrl, appPath);
        Path appLocalPath = Paths.get(Constant.localAppPath, appPath);

        // 检查是否有和该app md5一致的
        try {
            log.info("应用路径：{}", appLocalPath);
            if (Files.notExists(appLocalPath)) {
                Downloader d = new Downloader();
                d.start(appUrl, appLocalPath.toString());
            }
        } catch (DownloadFailException | IOException e) {
            e.printStackTrace();
        }
        return appLocalPath.toString();
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

    public void installApp(String deviceId, String appPath) {
        ADBCommandUtils.unlockWindow(deviceId);
        ADBCommandUtils.inputCode(deviceId, KeyCode.KEYCODE_HOME);
        log.info("[{}] 开始安装应用", deviceId);
        ADBCommandUtils.installApp(deviceId, appPath);
    }

}
