package com.testwa.distest.server.service.fdfs.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.exception.FdfsServerException;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class FdfsStorageService {

    @Autowired
    protected FastFileStorageClient fastFileStorageClient;
    @Autowired
    protected AppendFileStorageClient storageClient;

    private static final String GROUP = "group1";

    public StorePath simpleUpload(InputStream inputStream, Long fileSize, String fileExtName){
        StorePath path = storageClient.uploadFile(GROUP, inputStream, fileSize, fileExtName);
        log.info("上传文件-----{}", path.getFullPath());
        return path;
    }

    public void deleteFile(String path){
        fastFileStorageClient.deleteFile(path);
    }

}
