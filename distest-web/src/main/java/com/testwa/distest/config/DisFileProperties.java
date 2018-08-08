package com.testwa.distest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dis-file")
public class DisFileProperties {

    private String appium;
    private String app;
    private String script;
    private String logcat;
    private String screeshot;
    private String dist;

}
