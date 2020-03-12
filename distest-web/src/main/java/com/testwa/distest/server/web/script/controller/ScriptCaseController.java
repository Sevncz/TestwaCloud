package com.testwa.distest.server.web.script.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptActionEnum;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.service.script.form.ScriptCaseListForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSaveForm;
import com.testwa.distest.server.service.script.service.ScriptActionService;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptFunctionService;
import com.testwa.distest.server.service.script.service.ScriptMetadataService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(value = "脚本相关api", tags = "V2.0")
@Validated
@RestController
@RequestMapping(path = "/v2")
public class ScriptCaseController extends BaseController {

    @Autowired
    private ScriptFunctionService scriptFunctionService;
    @Autowired
    private ScriptCaseService scriptCaseService;
    @Autowired
    private ScriptActionService scriptActionService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptCode scriptCodePython;
    @Autowired
    private ScriptMetadataService scriptMetadataService;
    @Autowired
    private ScriptGenerator scriptGenerator;

    @ApiOperation(value = "创建一个脚本", notes = "")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/script/save")
    public ScriptCase save(@PathVariable Long projectId, @RequestBody ScriptCaseSaveForm form) {
        if(!ScriptCase.PLATFORM_ANDROID.equals(form.getPlatform()) && !ScriptCase.PLATFORM_IOS.equals(form.getPlatform())) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "平台只支持Android和iOS");
        }
        Project project = projectValidator.validateProjectExist(projectId);
        return scriptCaseService.saveCase(project, form);
    }

    @ApiOperation(value = "删除脚本")
    @ResponseBody
    @DeleteMapping(value = "/script/delete")
    public void delete(@RequestParam("scriptCaseId") String scriptCaseId) {

    }

    @ApiOperation(value = "脚本列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/script/all")
    public List<ScriptCase> listAll(@PathVariable Long projectId) {
        projectValidator.validateProjectExist(projectId);
        List<ScriptCase> scriptCases = scriptCaseService.list(projectId, new ScriptCaseListForm());
        return scriptCases;
    }

    @ApiOperation(value = "脚本分页列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/script/page")
    public PageInfo<ScriptCase> page(@PathVariable Long projectId, @Valid ScriptCaseListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        PageInfo<ScriptCase> scriptCaseResult = scriptCaseService.page(projectId, pageForm);
        return scriptCaseResult;
    }

    @ApiOperation(value = "脚本详情", notes = "")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/detail")
    public ScriptCaseVO detail(@PathVariable String scriptCaseId) {
        return scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
    }

    @ApiOperation(value = "脚本详情", notes = "")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/py")
    public String py(@PathVariable String scriptCaseId) {
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
        List<Function> templateFunctions = getFunctions(scriptCaseDetailVO);
        String scriptContent = "";
        if(ScriptCase.PLATFORM_ANDROID.equals(scriptCaseDetailVO.getPlatform())) {
            String deviceId = "xxxx";
            String platformVersion = "9";
            String appPath = "/app/path";
            String port = "4723";
            scriptContent = scriptGenerator.toAndroidPyScript(templateFunctions, deviceId, platformVersion, appPath, port);
        }
        if(ScriptCase.PLATFORM_IOS.equals(scriptCaseDetailVO.getPlatform())) {
            String udid = "udid";
            String platformVersion = "13.3";
            String xcodeOrgId = "xcodeOrgId";
            String appPath = "/app/path";
            String port = "4723";
            scriptContent = scriptGenerator.toIosPyScript(templateFunctions, udid, xcodeOrgId, platformVersion, appPath, port, "8100", "9100");
        }
        return scriptContent;
    }

    @ApiOperation(value = "脚本action code", notes = "")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/pyActionCode")
    public List<Function> pyActionCode(@PathVariable String scriptCaseId) {
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
        return getFunctions(scriptCaseDetailVO);
    }

    private List<Function> getFunctions(ScriptCaseVO scriptCaseDetailVO) {
        Map<String, String> map = scriptMetadataService.getPython();

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
