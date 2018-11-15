package com.testwa.distest.server.web.testcase.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.form.IDForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 16/9/2.
 */
@Slf4j
@Api("测试案例相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class TestcaseController extends BaseController {
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private UserService userService;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private AppValidator appValidator;

    @ResponseBody
    @PostMapping(value = "/project/{projectId}/saveCase")
    public Long save(@PathVariable Long projectId, @RequestBody @Valid TestcaseNewForm form) {
        projectValidator.validateProjectExist(projectId);

        scriptValidator.validateScriptsInProject(form.getScriptIds(), projectId);

        AppInfo appInfo = appValidator.validateAppInfoExist(form.getAppInfoId());
        scriptValidator.validateScriptBelongApp(form.getScriptIds(), appInfo.getPackageName());

        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());

        return testcaseService.saveFunctionalTestcase(projectId, appInfo, form);
    }

    @ResponseBody
    @PostMapping(value = "/case/modify")
    public void modify(@RequestBody @Valid TestcaseUpdateForm form) {
        Testcase testcase = testcaseValidatoer.validateTestcaseExist(form.getTestcaseId());

        scriptValidator.validateScriptsInProject(form.getScriptIds(), testcase.getProjectId());

        AppInfo appInfo = appValidator.validateAppInfoExist(form.getAppInfoId());

        scriptValidator.validateScriptBelongApp(form.getScriptIds(), appInfo.getPackageName());

        testcaseService.update(form);
    }

    @ResponseBody
    @PostMapping(value = "/case/deleteAll")
    public void deleteAll(@RequestBody @Valid IDListForm form) {
        testcaseService.delete(form.getEntityIds());
    }

    @ResponseBody
    @PostMapping(value = "/case/deleteOne")
    public void deleteOne(@RequestBody @Valid IDForm form) {
        testcaseService.delete(form.getEntityId());
    }

    @ResponseBody
    @GetMapping(value = "/case/{caseId}/detail")
    public TestcaseVO detail(@PathVariable Long caseId){
        testcaseValidatoer.validateTestcaseExist(caseId);
        return testcaseService.getTestcaseVO(caseId);
    }

    @ResponseBody
    @GetMapping(value = "/project/{projectId}/caseList")
    public List caseList(@PathVariable Long projectId, @Valid TestcaseListForm listForm) {
        projectValidator.validateProjectExist(projectId);
        List<Testcase> testcases = testcaseService.findList(projectId, listForm);
        return buildVOs(testcases, TestcaseVO.class);
    }

    @ResponseBody
    @GetMapping(value = "/project/{projectId}/casePage")
    public PageResult casePage(@PathVariable Long projectId, @Valid TestcaseListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        PageResult<Testcase> testcasePR = testcaseService.findPage(projectId, pageForm);
        return buildVOPageResult(testcasePR, TestcaseVO.class);
    }

    @ResponseBody
    @GetMapping(value = "/project/{projectId}/caseListByScripts/{scriptIds}")
    public List caseListByScript(@PathVariable Long projectId, @PathVariable String scriptIds) {
        projectValidator.validateProjectExist(projectId);
        String[] s = scriptIds.split(",");
        List<Long> ids = new ArrayList<>();
        for(String id_str : s) {
            try {
                Long idLong = Long.parseLong(id_str);
                ids.add(idLong);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "参数格式错误");
            }
        }
        return testcaseService.findByScripts(ids);
    }

}
