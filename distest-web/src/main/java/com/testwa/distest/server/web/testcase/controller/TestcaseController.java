package com.testwa.distest.server.web.testcase.controller;

import com.testwa.core.base.vo.Result;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by wen on 16/9/2.
 */
@Log4j2
@Api("测试案例相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/case")
public class TestcaseController extends BaseController {
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptValidator scriptValidator;

    @ResponseBody
    @PostMapping(value = "/save")
    public Result save(@Valid @RequestBody TestcaseNewForm form) throws ObjectNotExistsException, AccountException {
        log.info(form.toString());
        scriptValidator.validateScriptsExist(form.getScriptIds());
        projectValidator.validateProjectExist(form.getProjectId());
        testcaseService.saveRegressionTestcase(form);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid @RequestBody TestcaseUpdateForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        scriptValidator.validateScriptsExist(form.getScriptIds());
        testcaseService.update(form);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/delete")
    public Result delete(@Valid @RequestBody DeleteAllForm form) {
        log.info(form.toString());
        testcaseService.delete(form.getEntityIds());
        return ok();
    }

    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid TestcaseListForm pageForm) throws ObjectNotExistsException, AccountException {
        log.info(pageForm.toString());
        PageResult<Testcase> testcasePR = testcaseService.findPage(pageForm);
        PageResult<TestcaseVO> pr = buildVOPageResult(testcasePR, TestcaseVO.class);
        return ok(pr);
    }

    @ResponseBody
    @GetMapping(value = "/detail/{caseId}")
    public Result detail(@PathVariable Long caseId){
        TestcaseVO testcaseVO = testcaseService.getTestcaseVO(caseId);
        return ok(testcaseVO);
    }

    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(@Valid TestcaseListForm listForm) throws ObjectNotExistsException, AccountException {
        List<Testcase> testcases = testcaseService.find(listForm);
        List<TestcaseVO> vos = buildVOs(testcases, TestcaseVO.class);
        return ok(vos);
    }

}
