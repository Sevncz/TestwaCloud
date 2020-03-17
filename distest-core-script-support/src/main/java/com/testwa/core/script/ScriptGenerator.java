package com.testwa.core.script;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScriptGenerator {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    public String toIosPyScript(List<List<Function>> cases, String udid, String xcodeOrgId, String platformVersion, String app, String appiumPort, String wdaLocalPort, String mjpegServerPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("cases", cases);
        model.put("udid", udid);
        model.put("xcodeOrgId", xcodeOrgId);
        model.put("platformVersion", platformVersion);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "iOS");
        model.put("wdaLocalPort", StringUtil.isBlank(wdaLocalPort)?"8100":wdaLocalPort);
        model.put("mjpegServerPort", StringUtil.isBlank(mjpegServerPort)?"9100":mjpegServerPort);
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_template.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("iOS脚本生成失败", e);
        }
        return null;
    }

    public String toAndroidPyScript(List<List<Function>> cases, String deviceName, String platformVersion, String app, String appiumPort, String systemPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("cases", cases);
        model.put("platformVersion", platformVersion);
        model.put("deviceName", deviceName);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "Android");
        model.put("systemPort", systemPort);
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_template.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("Android脚本生成失败", e);
        }
        return null;
    }

    public String toPyClassScript(List<Function> functions) {
        Map<String, Object> model = new HashMap<>();
        model.put("cases", Collections.singleton(functions));

        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_class_template.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("Android脚本生成失败", e);
        }
        return null;
    }

    public String toIOSPyHeaderScript(String deviceName, String xcodeOrgId, String platformVersion, String app, String appiumPort, String wdaLocalPort, String mjpegServerPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("platformVersion", platformVersion);
        model.put("deviceName", deviceName);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "iOS");
        model.put("xcodeOrgId", xcodeOrgId);
        model.put("wdaLocalPort", StringUtil.isBlank(wdaLocalPort)?"8100":wdaLocalPort);
        model.put("mjpegServerPort", StringUtil.isBlank(mjpegServerPort)?"9100":mjpegServerPort);
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_header_template.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("Android脚本生成失败", e);
        }
        return null;
    }

    public String toAndroidPyHeaderScript(String deviceName, String platformVersion, String app, String appiumPort) {
        Map<String, Object> model = new HashMap<>();
        model.put("platformVersion", platformVersion);
        model.put("deviceName", deviceName);
        model.put("appPath", app);
        model.put("port", appiumPort);
        model.put("type", "Android");
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_header_template.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("Android脚本生成失败", e);
        }
        return null;
    }

}
