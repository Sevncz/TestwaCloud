package com.testwa.distest.server.web.script.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptCaseSetListForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSetSaveForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSetUpdateForm;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.entity.ScriptCaseSet;
import com.testwa.distest.server.service.script.service.ScriptCaseSetService;
import com.testwa.distest.server.web.script.validator.ScriptCaseSetValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by wen on 16/9/2.
 */
@Slf4j
@Api(value = "测试集相关api", tags = "V2.0")
@Validated
@RestController
@RequestMapping(path = "/v2")
public class ScriptCaseSetController extends BaseController {

    @Autowired
    private ScriptCaseSetService scriptCaseSetService;
    @Autowired
    private ScriptCaseService scriptCaseService;
    @Autowired
    private ScriptCaseSetValidator scriptCaseSetValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value = "创建脚本测试集", notes = "")
    @PostMapping(value = "/project/{projectId}/scriptCaseSet/save")
    public String save(@PathVariable Long projectId, @RequestBody ScriptCaseSetSaveForm scriptCaseSetSaveForm) {
        Project project = projectValidator.validateProjectExist(projectId);
        scriptCaseSetService.save(project, scriptCaseSetSaveForm);
        return "保存成功";
    }

    @ApiOperation(value = "创建空的脚本测试集", notes = "")
    @PostMapping(value = "/project/{projectId}/scriptCaseSet/simpleSave")
    public String simpleSave(@PathVariable Long projectId, @RequestBody ScriptCaseSetSaveForm scriptCaseSetSaveForm) {
        Project project = projectValidator.validateProjectExist(projectId);
        scriptCaseSetService.saveSimple(project, scriptCaseSetSaveForm.getCaseName());
        return "保存成功";
    }

    @ApiOperation(value = "测试集分页列表", notes = "")
    @GetMapping(value = "/project/{projectId}/scriptCaseSet/page")
    public PageInfo<ScriptCaseSet> page(@PathVariable Long projectId, @Valid ScriptCaseSetListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        PageInfo<ScriptCaseSet> pageResult = scriptCaseSetService.page(projectId, pageForm);
        return pageResult;
    }

    @ApiOperation(value = "删除脚本测试集", notes = "")
    @DeleteMapping(value = "/project/{projectId}/scriptCaseSet/delete")
    public String delete(@RequestParam("entityIds") Long[] entityIds) {
        scriptCaseSetService.deleteAll(entityIds);
        return "删除成功";
    }

    @ApiOperation(value = "更新脚本测试集", notes = "")
    @PutMapping(value = "/project/{projectId}/scriptCaseSet/update")
    public String update(@PathVariable Long projectId, @RequestBody ScriptCaseSetUpdateForm form) {
        Project project = projectValidator.validateProjectExist(projectId);
        scriptCaseSetService.update(form);
        return "更新成功";
    }

    @ApiOperation(value = "测试集下的脚本列表", notes = "")
    @GetMapping(value = "/project/{projectId}/scriptCaseSet/{scriptCaseId}/script")
    public List<ScriptCase> scriptList(@PathVariable Long projectId, @PathVariable String scriptCaseId) {
        Project project = projectValidator.validateProjectExist(projectId);
        ScriptCaseSet scriptCaseSet = scriptCaseSetValidator.validateScriptCaseSetIdExist(scriptCaseId);
        String scriptCaseIdStr = scriptCaseSet.getScriptCaseIds();
        List<String> scriptCaseIds = JSON.parseArray(scriptCaseIdStr, String.class);
        return scriptCaseService.listByScriptCaseId(scriptCaseIds);
    }

}
