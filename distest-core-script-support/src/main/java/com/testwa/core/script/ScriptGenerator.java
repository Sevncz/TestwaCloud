package com.testwa.core.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.testwa.core.script.snippet.ScriptActionEnum;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.util.VoUtil;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class ScriptGenerator {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private ScriptCode scriptCodePython;

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

    public String toAndroidPyScript(List<ScriptCaseVO> cases, List<List<Function>> templateFunctions, String deviceName, String platformVersion, String app, String appiumPort, String systemPort) {

        Map<String, Object> model = new HashMap<>();
        model.put("cases", cases);
        model.put("functions", templateFunctions);
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
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_class.ftl");
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
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_header.ftl");
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
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("test_py_header.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error("Android脚本生成失败", e);
        }
        return null;
    }


    public List<Function> getFunctions(ScriptCaseVO scriptCaseDetailVO, Map<String, String> strategyMap) {
        List<ScriptFunctionVO> functionList = scriptCaseDetailVO.getFunctions();
        List<Function> templateFunctions = new ArrayList<>();
        for (ScriptFunctionVO scriptFunctionVO : functionList) {
            List<ScriptActionVO> actionVOS = scriptFunctionVO.getActions();
            Function function = VoUtil.buildVO(scriptFunctionVO, Function.class);
            function.setActions(null);
            function.setScriptCaseId(scriptCaseDetailVO.getScriptCaseId());
            for (ScriptActionVO scriptActionVO : actionVOS) {
                String code = "";
                String action = scriptActionVO.getAction();
                JSONArray jsonArray = JSON.parseArray(scriptActionVO.getParameter());
                if (ScriptActionEnum.findAndAssign.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_findAndAssign(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2), jsonArray.getBoolean(3), strategyMap);
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
                if (ScriptActionEnum.sendKeys.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_sendKeys(jsonArray.getString(0), jsonArray.getString(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ScriptActionEnum.swipe.name().equals(action)) {
                    try {
                        code = scriptCodePython.codeFor_swipe(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2), jsonArray.getString(3), jsonArray.getString(4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                function.addCode(code);
            }
            templateFunctions.add(function);
        }
        return templateFunctions;
    }
}
