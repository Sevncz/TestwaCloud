package com.testwa.distest.server.web.common;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.service.fdfs.service.FdfsStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/fileSupport")
@Validated
public class FileSupportController {

    @Autowired
    private FdfsStorageService fdfsStorageService;

    @PostMapping("/single")
    @ApiOperation(value = "上传一个文件", notes = "上传一个文件", httpMethod = "POST")
    public String singleFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws BusinessException {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "上传的文件大小为空,请检查!!");
        }
        //获取文件名称、后缀名、大小
        String fileName = file.getOriginalFilename();
        if(StringUtils.isBlank(fileName)) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "文件名为空,请检查!!");
        }
        String[] names = fileName.split("\\.");
        String suffixName = names[names.length - 1];
        long size = file.getSize();

        String path = "";
        try {
            StorePath storePath = fdfsStorageService.simpleUpload(file.getInputStream(), size, suffixName);
            path = storePath.getFullPath();
        } catch (IOException e) {
            log.error("上传的文件异常", e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "上传的文件异常!!");
        }
        return path;
    }

    @DeleteMapping("/single")
    @ApiOperation(value = "删除一个已上传的文件", notes = "删除一个已上传的文件", httpMethod = "DELETE")
    public Boolean deleteFile(@RequestParam("path") String path) {
        Boolean success = fdfsStorageService.deleteFile(path);
        return success;
    }

    @PostMapping("/multi")
    @ApiOperation(value = "上传多个文件", notes = "上传多个文件", httpMethod = "POST", hidden = true)
    public List<String> file(HttpServletRequest request) throws BusinessException {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("files");
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new BusinessException(ResultCode.ILLEGAL_PARAM, "上传的文件大小为空,请检查!!");
            }
        }
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            //获取文件名称、后缀名、大小
            String fileName = file.getOriginalFilename();
            if(StringUtils.isBlank(fileName)) {
                throw new BusinessException(ResultCode.ILLEGAL_PARAM, "文件名为空,请检查!!");
            }
            String[] names = fileName.split("\\.");
            String suffixName = names[names.length - 1];
            long size = file.getSize();

            String path = "";
            try {
                StorePath storePath = fdfsStorageService.simpleUpload(file.getInputStream(), size, suffixName);
                path = storePath.getPath();
                result.add(path);
            } catch (IOException e) {
                log.error("上传的文件异常", e);
                throw new BusinessException(ResultCode.SERVER_ERROR, "上传的文件异常!!");
            }
        }
        return result;
    }

}
