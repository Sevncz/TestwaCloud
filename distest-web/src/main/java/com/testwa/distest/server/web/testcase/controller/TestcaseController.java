package com.testwa.distest.server.web.testcase.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.server.mvc.beans.PageResult;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by wen on 16/9/2.
 */
@Api("测试案例相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/case")
public class TestcaseController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TestcaseController.class);

    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptValidator scriptValidator;

    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result save(@Valid TestcaseNewForm form) throws NoSuchProjectException, NoSuchScriptException, AccountException {
        log.info(form.toString());
        scriptValidator.validateScriptsExist(form.getScriptIds());
        projectValidator.validateProjectExist(form.getProjectId());
        testcaseService.saveRegressionTestcase(form);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid TestcaseUpdateForm form) throws NoSuchProjectException, NoSuchScriptException, NoSuchTestcaseException{
        log.info(form.toString());
        scriptValidator.validateScriptsExist(form.getScriptIds());
        testcaseService.update(form);
        return ok();
    }

    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result delete(@Valid DeleteAllForm form) {
        log.info(form.toString());
        testcaseService.delete(form.getEntityIds());
        return ok();
    }

    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(TestcaseListForm pageForm) throws NotInProjectException, AccountException {
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
    public Result list(TestcaseListForm listForm) throws NotInProjectException, AccountException {
        List<Testcase> testcases = testcaseService.find(listForm);
        List<TestcaseVO> vos = buildVOs(testcases, TestcaseVO.class);
        return ok(vos);
    }

}
