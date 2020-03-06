package com.testwa.core.script;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScriptGenerator {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    public String toIosPyScript(List<Action> actions, String udid, String xcodeOrgId, String platformVersion, String app, String appiumPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("actions", actions);
        model.put("udid", udid);
        model.put("xcodeOrgId", xcodeOrgId);
        model.put("platformVersion", platformVersion);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "iOS");
        try {
            Template template = getPyTemplate();
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("脚本生成失败", e);
        }
        return null;
    }

    public String toAndroidPyScript(List<Action> actions, String platformVersion, String app, String appiumPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("actions", actions);
        model.put("platformVersion", platformVersion);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "Android");
        try {
            Template template = getPyTemplate();
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("脚本生成失败", e);
        }
        return null;
    }

    private Template getPyTemplate() throws IOException {
        return freeMarkerConfigurer.getConfiguration().getTemplate("test_py_template.ftl");
    }

}
