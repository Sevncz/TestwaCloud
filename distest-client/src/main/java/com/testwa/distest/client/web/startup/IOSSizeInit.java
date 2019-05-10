package com.testwa.distest.client.web.startup;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.ios.IOSPhysicalSize;
import com.testwa.distest.client.ios.IOSSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wen
 * @create 2019-05-10 14:18
 */
@Slf4j
@Order(value=1)
@Component
public class IOSSizeInit implements CommandLineRunner {
    @Value("${config.iphone.size}")
    private String jsonFile;

    private ResourceLoader resourceLoader;

    @Autowired
    public IOSSizeInit(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... strings) throws Exception {
        loadSize();
    }

    public void loadSize() throws Exception {
        Resource resource = resourceLoader.getResource(jsonFile);
        InputStream inputStream = resource.getInputStream();
        String result = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining(""));
        IOSDeviceUtil.loadSize(result);
    }
}
