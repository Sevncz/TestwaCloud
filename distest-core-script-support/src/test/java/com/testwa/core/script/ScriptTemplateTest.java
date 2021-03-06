package com.testwa.core.script;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScriptTemplateTest {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private ScriptGenerator scriptGenerator;

    @Test
    public void iosTemplateFreemarkTest() throws IOException, TemplateException {
        List<Function> functionList = new ArrayList<>();
        Function function = new Function();
        function.setFeature("模版测试");
        List<String> actions = new ArrayList<>();
        actions.add("el = driver.find_element_by_accessibility_id('show alert')");
        actions.add("el.click()");
        function.setActions(actions);
        function.setSeverity("trivial");
        function.setTitle("模版测试弹框");
        functionList.add(function);
        Map<String, Object> model = new HashMap<>();
        model.put("actions", functionList);
        model.put("udid", "5a94a4eefd68f77083a73e4ca73079ce0eebdcf7");
        model.put("xcodeOrgId", "xcodeOrgId");
        model.put("platformVersion", "13.3");
        model.put("appPath", "/Users/wen/dev/TestApp.zip");
        model.put("port", "4723");
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_template.ftl");
        String scriptContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        System.out.println(scriptContent);
    }

    @Test
    public void iosTemplateTest() {
        List<Function> functionList = new ArrayList<>();
        Function function = new Function();
        function.setFeature("模版测试");
        List<String> actions = new ArrayList<>();
        actions.add("el = driver.find_element_by_accessibility_id('show alert')");
        actions.add("el.click()");
        function.setActions(actions);
        function.setSeverity("trivial");
        function.setTitle("模版测试弹框");
        functionList.add(function);

        String udid = "5a94a4eefd68f77083a73e4ca73079ce0eebdcf7";
        String platformVersion = "13.3";
        String xcodeOrgId = "xcodeOrgId";
        String appPath = "/Users/wen/dev/TestApp.zip";
        String port = "4723";
        String scriptContent = scriptGenerator.toIosPyScript(functionList, udid, xcodeOrgId, platformVersion, appPath, port);
        System.out.println(scriptContent);
    }

}
