package com.testwa.core.script;

import com.testwa.core.script.Function;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class EnvGenerator {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    public String getPyEnvContent() {
        Properties props=System.getProperties(); //获得系统属性集
        String osName = props.getProperty("os.name"); //操作系统名称
        String osVersion = props.getProperty("os.version"); //操作系统版本
        String javaVersion = props.getProperty("java.version"); //Java 版本

        Map<String, Object> model = new HashMap<>();
        model.put("osName", osName);
        model.put("osVersion", osVersion);
        model.put("javaversion", javaVersion);
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("py_environment.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("iOS脚本生成失败", e);
        }
        return null;
    }

}
