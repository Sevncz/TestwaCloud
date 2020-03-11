package com.testwa.distest.server.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptActionEnum;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptMetadataService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class ScriptTemplateTest {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private ScriptGenerator scriptGenerator;
    @Autowired
    private ScriptCaseService scriptCaseService;
    @Autowired
    private ScriptCode scriptCodePython;
    @Autowired
    private ScriptMetadataService scriptMetadataService;

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
        String scriptContent = scriptGenerator.toIosPyScript(functionList, udid, xcodeOrgId, platformVersion, appPath, port, "8100", "9100");
        System.out.println(scriptContent);
    }

    @Test
    public void iosScriptCaseTemplateTest() {
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO("245947650254438400");
        List<ScriptFunctionVO> functionList = scriptCaseDetailVO.getFunctions();
        Map<String, String> map = scriptMetadataService.getPython();
        List<Function> templateFunctions = new ArrayList<>();
        for (ScriptFunctionVO scriptFunctionVO : functionList) {
            List<ScriptActionVO> actionVOS = scriptFunctionVO.getActions();
            Function function = VoUtil.buildVO(scriptFunctionVO, Function.class);
            function.setActions(null);
            for (ScriptActionVO scriptActionVO : actionVOS) {
                String code = "";
                String action = scriptActionVO.getAction();
                JSONArray jsonArray = JSON.parseArray(scriptActionVO.getParameter());
                if (ScriptActionEnum.findAndAssign.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_findAndAssign(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2), jsonArray.getBoolean(3), map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.click.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_click(jsonArray.getString(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.tap.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_tap(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                function.addCode(code);
            }
            templateFunctions.add(function);
        }

        String udid = "5a94a4eefd68f77083a73e4ca73079ce0eebdcf7";
        String platformVersion = "13.3";
        String xcodeOrgId = "xcodeOrgId";
        String appPath = "/Users/wen/dev/TestApp.zip";
        String port = "4723";
        String scriptContent = scriptGenerator.toIosPyScript(templateFunctions, udid, xcodeOrgId, platformVersion, appPath, port, "8100", "9100");
        log.info(scriptContent);
    }

}
