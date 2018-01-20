package com.testwa.distest.common.validator;

import com.testwa.core.base.exception.ParamsFormatException;
import com.testwa.core.base.exception.ParamsIsNullException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
public class FileUploadValidator {

    protected static final Logger LOG = LoggerFactory.getLogger(FileUploadValidator.class);

    /**
     * 验证文件大小，文件名，文件后缀
     *
     * @param file
     * @param maxLength
     * @param allowExtName
     */
    public void validateFile(MultipartFile file, long maxLength, String[] allowExtName) throws ParamsIsNullException, ParamsFormatException {

        if (file.isEmpty()) {
            throw new ParamsIsNullException("您没有上传文件");
        }

        // 文件大小
        if (file.getSize() < 0 || file.getSize() > maxLength) {
            throw new ParamsFormatException("文件不允许超过" + String.valueOf(maxLength));
        }

        //
        // 处理不选择文件点击上传时，也会有MultipartFile对象，在此进行过滤
        //
        String filename = file.getOriginalFilename();

        if (StringUtils.isBlank(filename)) {
            throw new ParamsIsNullException("文件名不能为空");
        }

        //
        // 文件名后缀
        //

        if (filename.contains(".")) {

            String extName = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            if (allowExtName == null || allowExtName.length == 0 || Arrays.binarySearch(allowExtName, extName) >= 0) {
            } else {
                throw new ParamsFormatException("文件后缀不允许");
            }
        } else {
            throw new ParamsFormatException("文件后缀不允许");
        }
    }

    /**
     * 验证文件大小，文件名，文件后缀
     *
     * @param files
     * @param maxLength
     * @param allowExtName
     *
     * @return
     */
    public List<MultipartFile> validateFiles(List<MultipartFile> files, long maxLength, String[] allowExtName) {

        List<MultipartFile> retFiles = new ArrayList<MultipartFile>();

        for (MultipartFile multipartFile : files) {

            try {

                this.validateFile(multipartFile, maxLength, allowExtName);
                retFiles.add(multipartFile);

            } catch (Exception e) {

                LOG.warn(e.toString());
            }
        }

        return retFiles;
    }
}
