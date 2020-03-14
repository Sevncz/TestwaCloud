package com.testwa.distest.server.web.script.controller;

import com.github.pagehelper.PageInfo;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptCaseSetListForm;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.entity.ScriptCaseSet;
import com.testwa.distest.server.service.script.service.ScriptCaseSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

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
    private ProjectValidator projectValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value = "创建脚本测试集", notes = "")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/scriptCaseSet/save")
    public String save(ScriptCaseSet scriptCaseSet) {
        scriptCaseSet.setCreateBy(currentUser.getId());
        scriptCaseSet.setCreateTime(new Date());
        scriptCaseSetService.insert(scriptCaseSet);
        return "保存成功";
    }

    @ApiOperation(value = "测试集分页列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/scriptCaseSet/page")
    public PageInfo<ScriptCaseSet> page(@PathVariable Long projectId, @Valid ScriptCaseSetListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        PageInfo<ScriptCaseSet> pageResult = scriptCaseSetService.page(projectId, pageForm);
        return pageResult;
    }

    @ApiOperation(value = "删除脚本测试集", notes = "")
    @ResponseBody
    @DeleteMapping(value = "/scriptCaseSet/delete")
    public String delete(@RequestParam("entityId") String entityId) {

        return "删除成功";
    }

}
