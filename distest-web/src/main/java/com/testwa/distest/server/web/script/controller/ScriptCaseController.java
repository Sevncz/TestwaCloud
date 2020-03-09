package com.testwa.distest.server.web.script.controller;


import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ScriptAction;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.ScriptFunction;
import com.testwa.distest.server.service.script.form.ScriptCaseListForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSaveForm;
import com.testwa.distest.server.service.script.service.ScriptActionService;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptFunctionService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.vo.ScriptActionVO;
import com.testwa.distest.server.web.script.vo.ScriptCaseVO;
import com.testwa.distest.server.web.script.vo.ScriptFunctionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @ApiOperation(value="创建一个脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/script/save")
    public String save(@PathVariable Long projectId, @RequestBody ScriptCaseSaveForm form) {
        Project project = projectValidator.validateProjectExist(projectId);

        scriptCaseService.saveCase(project, form);
        return "保存成功";
    }

    @ApiOperation(value="脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/script/all")
    public List<ScriptCase> listAll(@PathVariable Long projectId) {
        projectValidator.validateProjectExist(projectId);
        List<ScriptCase> scriptCases = scriptCaseService.list(projectId, new ScriptCaseListForm());
        return scriptCases;
    }

    @ApiOperation(value="脚本详情", notes="")
    @ResponseBody
    @GetMapping(value = "/script/{scriptCaseId}/detail")
    public ScriptCaseVO detail(@PathVariable String scriptCaseId) {
        return scriptCaseService.getScriptCaseDetailVO(scriptCaseId);
    }


}
