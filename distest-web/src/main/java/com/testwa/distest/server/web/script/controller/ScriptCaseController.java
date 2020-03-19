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
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.service.script.form.ScriptCaseListForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSaveForm;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
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
    private ScriptCaseService scriptCaseService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptGenerator scriptGenerator;
    @Autowired
    private ScriptMetadataService scriptMetadataService;

    @ApiOperation(value = "创建一个脚本", notes = "")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/script/save")
    public ScriptCase save(@PathVariable Long projectId, @RequestBody ScriptCaseSaveForm form) {
        if (!ScriptCase.PLATFORM_ANDROID.equals(form.getPlatform()) && !ScriptCase.PLATFORM_IOS.equals(form.getPlatform())) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "平台只支持Android和iOS");
        }
        Project project = projectValidator.validateProjectExist(projectId);
        return scriptCaseService.saveCase(project, form);
    }

    @ApiOperation(value = "删除脚本")
    @ResponseBody
    @DeleteMapping(value = "/script/delete")
    public void delete(@RequestParam("scriptCaseId") String scriptCaseId) {
        ScriptCase scriptCase = scriptCaseService.getByScriptCaseId(scriptCaseId);
        scriptCaseService.disable(scriptCase.getId());
    }

    @ApiOperation(value = "脚本列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/script/all")
    public List<ScriptCase> listAll(@PathVariable Long projectId,
                                    @RequestParam(required = false, name = "basePackage") String basePackage) {
        projectValidator.validateProjectExist(projectId);
        List<ScriptCase> scriptCases = scriptCaseService.list(projectId, new ScriptCaseListForm(), basePackage);
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
        Map<String, String> map = scriptMetadataService.getPython();
        return scriptGenerator.toPyClassScript(scriptCaseDetailVO, map);
    }

    @ApiOperation(value = "脚本头", notes = "")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/header")
    public String header(@PathVariable String scriptCaseId) {
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
        String scriptContent = "";
        if (ScriptCase.PLATFORM_ANDROID.equals(scriptCaseDetailVO.getPlatform())) {
            String deviceId = "xxxx";
            String platformVersion = "9";
            String appPath = "/app/path";
            String port = "4723";
            scriptContent = scriptGenerator.toAndroidPyHeaderScript(deviceId, platformVersion, appPath, port);
        }
        if (ScriptCase.PLATFORM_IOS.equals(scriptCaseDetailVO.getPlatform())) {
            String udid = "udid";
            String platformVersion = "13.3";
            String xcodeOrgId = "xcodeOrgId";
            String appPath = "/app/path";
            String port = "4723";
            scriptContent = scriptGenerator.toIOSPyHeaderScript(udid, xcodeOrgId, platformVersion, appPath, port, "8100", "9100");
        }

        return scriptContent;
    }

    @ApiOperation(value = "脚本action code", notes = "")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/pyActionCode")
    public List<Function> pyActionCode(@PathVariable String scriptCaseId) {
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
        Map<String, String> map = scriptMetadataService.getPython();
        return scriptGenerator.getFunctions(scriptCaseDetailVO, map);
    }


}
