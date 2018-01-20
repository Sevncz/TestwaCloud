package com.testwa.distest.server.web.testcase.controller;

import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.form.DeleteOneForm;
import com.testwa.core.base.vo.Result;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
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
    private UserService userService;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ScriptValidator scriptValidator;

    @ResponseBody
    @PostMapping(value = "/save")
    public Result save(@Valid @RequestBody TestcaseNewForm form) throws ObjectNotExistsException, AccountException, AuthorizedException {
        log.info(form.toString());
        scriptValidator.validateScriptsInProject(form.getScriptIds(), form.getProjectId());
        projectValidator.validateProjectExist(form.getProjectId());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());

        testcaseService.saveRegressionTestcase(form);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid @RequestBody TestcaseUpdateForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        scriptValidator.validateScriptsInProject(form.getScriptIds(), form.getProjectId());
        testcaseValidatoer.validateTestcaseExist(form.getTestcaseId());
        testcaseService.update(form);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/delete/all")
    public Result deleteAll(@Valid @RequestBody DeleteAllForm form) throws ParamsIsNullException {
        log.info(form.toString());
        if(form.getEntityIds() == null && form.getEntityIds().size() == 0){
            throw new ParamsIsNullException("参数不能为空");
        }
        testcaseService.delete(form.getEntityIds());
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/delete/one")
    public Result deleteOne(@Valid @RequestBody DeleteOneForm form) throws ParamsIsNullException {
        log.info(form.toString());
        if(form.getEntityId() == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        testcaseService.delete(form.getEntityId());
        return ok();
    }

    /**
     * 获得当前用户可见的测试案例分页列表
     * @param pageForm
     * @return
     * @throws ObjectNotExistsException
     * @throws AccountException
     */
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid TestcaseListForm pageForm) throws ObjectNotExistsException, AccountException {
        log.info(pageForm.toString());
        PageResult<Testcase> testcasePR = testcaseService.findPageForCurrentUser(pageForm);
        PageResult<TestcaseVO> pr = buildVOPageResult(testcasePR, TestcaseVO.class);
        return ok(pr);
    }

    @ResponseBody
    @GetMapping(value = "/detail/{caseId}")
    public Result detail(@PathVariable Long caseId){
        TestcaseVO testcaseVO = testcaseService.getTestcaseVO(caseId);
        return ok(testcaseVO);
    }

    /**
     * 获得当前用户可见的测试案例列表
     * @param listForm
     * @return
     * @throws ObjectNotExistsException
     * @throws AccountException
     */
    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(@Valid TestcaseListForm listForm) throws ObjectNotExistsException, AccountException {
        List<Testcase> testcases = testcaseService.findForCurrentUser(listForm);
        List<TestcaseVO> vos = buildVOs(testcases, TestcaseVO.class);
        return ok(vos);
    }

}
