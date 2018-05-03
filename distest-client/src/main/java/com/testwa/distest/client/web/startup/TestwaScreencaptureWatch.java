package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.task.CronScheduled;
import com.testwa.core.os.filewatch.FileActionCallback;
import com.testwa.core.os.filewatch.WatchDir;
import com.testwa.distest.client.component.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by wen on 16/9/4.
 */
@Component
public class TestwaScreencaptureWatch implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(TestwaScreencaptureWatch.class);

    @Override
    public void run(String... strings) throws Exception {
        final File file = new File(Constant.localScreenshotPath);
        new Thread(() -> {
            try {
                new WatchDir(file, true, new FileActionCallback() {
                    @Override
                    public void create(File file1) {
                        LOG.debug("文件已创建\t" + file1.getAbsolutePath());
                        try {
                            if(!file1.isDirectory()){
                                CronScheduled.screenUploadQueue.put(file1.getAbsolutePath());
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void delete(File file1) {
                        LOG.debug("文件已删除\t" + file1.getAbsolutePath());
                    }

                    @Override
                    public void modify(File file1) {
                        LOG.debug("文件已修改\t" + file1.getAbsolutePath());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        LOG.info("Watching folder:" + file.getAbsolutePath());
    }
}
