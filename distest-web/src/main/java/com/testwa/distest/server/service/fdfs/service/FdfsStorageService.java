package com.testwa.distest.server.service.fdfs.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.testwa.distest.server.entity.TaskResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FdfsStorageService {

    private static final String GROUP = "group1";
    @Autowired
    protected FastFileStorageClient fastFileStorageClient;
    @Autowired
    protected AppendFileStorageClient storageClient;

    public StorePath simpleUpload(InputStream inputStream, Long fileSize, String fileExtName) {
        StorePath path = storageClient.uploadFile(GROUP, inputStream, fileSize, fileExtName);
        log.info("上传文件-----{}", path.getFullPath());
        return path;
    }

    public void deleteFile(String path) {
        fastFileStorageClient.deleteFile(path);
    }

    @Async
    public CompletableFuture<Boolean> downloadResult(TaskResult result, Path... toDir) {
        DownloadByteArray callback = new DownloadByteArray();
        boolean success = true;
        byte[] b = fastFileStorageClient.downloadFile(GROUP, result.getUrl().replace(GROUP+"/", ""), callback);
        for(Path d : toDir) {
            Path resultFile = Paths.get(d.toString(), result.getResult());
            try {
                if(Files.notExists(resultFile)) {
                    Files.createFile(resultFile);
                }
                Files.write(resultFile, b);
            } catch (IOException e) {
                log.error("下载报告结果失败", e);
                success = false;
            }
        }
        return CompletableFuture.completedFuture(success);
    }
}
