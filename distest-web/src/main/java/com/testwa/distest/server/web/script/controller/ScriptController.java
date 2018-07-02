package com.testwa.distest.server.web.script.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptContentForm;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * Created by wen on 16/9/2.
 */
@Slf4j
@Api("脚本相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/script")
public class ScriptController extends BaseController {
    private static final String[] allowExtName = {".java", ".js", ".py", ".rb"};
    private static final long fileSize = 1024 * 100;  // 100k
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private ScriptValidator validator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private FileUploadValidator fileUploadValidator;
    @Autowired
    private TestcaseService testcaseService;

    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/upload")
    public Result upload(@RequestParam("file") MultipartFile uploadfile) throws ParamsIsNullException, ParamsFormatException, IOException {
        //
        // 校验
        //
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);

        Script script = scriptService.upload(uploadfile);
        return ok(script);
    }

    @ApiOperation(value="上传多个脚本", notes="暂时还不用")
    @ResponseBody
    @PostMapping(value = "/upload/multi")
    public Result uploadMulti(@RequestParam("files") List<MultipartFile> uploadfiles) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles);
        return ok();
    }

    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/{projectId}/upload")
    public Result upload(@PathVariable Long projectId, @RequestParam("file") MultipartFile uploadfile) throws ParamsIsNullException, ParamsFormatException, IOException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);
        Script script = scriptService.upload(uploadfile, projectId);
        return ok(script);
    }

    @ApiOperation(value="上传多个脚本")
    @ResponseBody
    @PostMapping(value = "/{projectId}/upload/multi")
    public Result uploadMulti(@PathVariable Long projectId, @RequestParam("files") List<MultipartFile> uploadfiles) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles, projectId);
        return ok();
    }

    @ApiOperation(value="上传的同时直接组成案例", notes="暂时还不用")
    @ResponseBody
    @PostMapping(value = "/{projectId}/upload/case")
    public Result uploadCase(@PathVariable Long projectId, @RequestParam("files") List<MultipartFile> uploadfiles) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles, projectId);
        //TODO 保存为案例
        return ok();
    }

    @ApiOperation(value="补充脚本信息", notes="")
    @ResponseBody
    @PostMapping(value = "/append")
    public Result appendInfo(@RequestBody @Valid ScriptUpdateForm form) throws ObjectNotExistsException, AuthorizedException {
        scriptValidator.validateScriptExist(form.getScriptId());
        projectValidator.validateProjectExist(form.getProjectId());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());
        scriptService.appendInfo(form);
        return ok();
    }

    @ApiOperation(value="删除脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/delete")
    public Result delete(@RequestBody @Valid IDListForm form) {
        // 检查脚本是否已被组合为测试集
        if(form.getEntityIds() != null && form.getEntityIds().size() == 0) {
            return ok();
        }
        List<Testcase> testcases = testcaseService.findByScripts(form.getEntityIds());
        if(testcases != null && testcases.size() > 0) {
            throw new ObjectAlreadyExistException("脚本已经存在测试集，无法删除");
        }
        scriptService.delete(form.getEntityIds());
        return ok();
    }

    @ApiOperation(value="脚本分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/page")
    public Result page(@PathVariable Long projectId, @Valid ScriptListForm queryForm) throws AuthorizedException {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());
        PageResult<Script> pr = scriptService.findPage(projectId, queryForm);
        return ok(pr);
    }

    @ApiOperation(value="脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/list")
    public Result list(@PathVariable Long projectId, @Valid ScriptListForm queryForm) throws AuthorizedException {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());
        List<Script> scriptList = scriptService.findList(projectId, queryForm);
        return ok(scriptList);
    }

    @ApiOperation(value="获得脚本内容", notes="")
    @ResponseBody
    @GetMapping(value = "/read/{scriptId}")
    public Result read(@PathVariable Long scriptId) throws IOException, ObjectNotExistsException {
        validator.validateScriptExist(scriptId);
        String content = scriptService.getContent(scriptId);
        return ok(content);
    }

    @ApiOperation(value="修改脚本内容", notes="")
    @ResponseBody
    @PostMapping(value = {"/write/{scriptId}"})
    public Result write(@PathVariable Long scriptId, @RequestBody ScriptContentForm form) throws IOException, ObjectNotExistsException {
        validator.validateScriptExist(scriptId);
        scriptService.modifyContent(scriptId, form.getContent());
        return ok();
    }


}
