package com.testwa.core.script.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Slf4j
public class FileUtil {

    public static Boolean ensureExistEmptyDir(String absPath) {
        File localDir = new File(absPath);
        try {
            // 确保存在空的project 文件夹
            if (!localDir.exists()) {
                localDir.mkdirs();
            } else {
                // 清空文件夹
                log.info("删除目录：");
                Files.walk(Paths.get(absPath)).sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .peek(System.out::println).forEach(File::delete);
                log.info("清空目录：" + absPath + "成功");
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }
}
