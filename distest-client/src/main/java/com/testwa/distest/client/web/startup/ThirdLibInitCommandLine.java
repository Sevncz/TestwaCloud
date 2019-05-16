package com.testwa.distest.client.web.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

/**
 * 第三方工具位置初始化
 *
 * @author wen
 * @create 2019-05-16 15:39
 */
@Slf4j
@Order(value=3)
@Component
public class ThirdLibInitCommandLine implements CommandLineRunner {
    @Value("${config.extra.path}")
    private String thirdLibPath;

    private ResourceLoader resourceLoader;

    @Autowired
    public ThirdLibInitCommandLine(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... strings) throws Exception {
        Resource[] resources = loadResources(thirdLibPath);
        for(int i=0; i<resources.length; i++) {
            log.info(resources[i].getURL().toString());
        }
    }

    private Resource[] loadResources(String pattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
    }
}
