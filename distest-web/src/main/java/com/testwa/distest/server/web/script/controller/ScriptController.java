package com.testwa.distest.server.web.script.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptContentForm;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class ScriptController extends BaseController {
    private static final String[] allowExtName = {".java", ".js", ".py", ".rb"};
    private static final long fileSize = 1024 * 100;  // 100k
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private ScriptValidator validator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private FileUploadValidator fileUploadValidator;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private User currentUser;

    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/script/upload")
    public Script upload(@RequestParam("file") MultipartFile uploadfile) throws IOException {
        //
        // 校验
        //
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);

        return scriptService.upload(uploadfile);
    }

    @ApiOperation(value="上传多个脚本", notes="暂时还不用")
    @ResponseBody
    @PostMapping(value = "/script/multi/upload")
    public void uploadMulti(@RequestParam("files") List<MultipartFile> uploadfiles) throws IOException {
        //
        // 校验
        //
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles);
    }

    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/script/upload")
    public Script upload(@PathVariable Long projectId, @RequestParam("file") MultipartFile uploadfile) throws IOException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);
        return scriptService.upload(uploadfile, projectId);
    }

    @ApiOperation(value="上传多个脚本")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/script/upload/multi")
    public void uploadMulti(@PathVariable Long projectId, @RequestParam("files") List<MultipartFile> uploadfiles) throws IOException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles, projectId);
    }

    @ApiOperation(value="上传的同时直接组成案例", notes="暂时还不用")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/upload/case")
    public void uploadCase(@PathVariable Long projectId, @RequestParam("files") List<MultipartFile> uploadfiles) throws IOException {
        //
        // 校验
        //
        projectValidator.validateProjectExist(projectId);
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles, projectId);
        //TODO 保存为案例
    }

    @ApiOperation(value="补充脚本信息", notes="")
    @ResponseBody
    @PostMapping(value = "/script/append")
    public void appendInfo(@RequestBody @Valid ScriptUpdateForm form) {
        scriptValidator.validateScriptExist(form.getScriptId());
        projectValidator.validateProjectExist(form.getProjectId());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), currentUser.getId());
        scriptService.appendInfo(form);
    }

    @ApiOperation(value="删除脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/script/delete")
    public void delete(@RequestBody @Valid IDListForm form) {
        List<Testcase> testcases = testcaseService.findByScripts(form.getEntityIds());
        if(testcases != null && !testcases.isEmpty()) {
            throw new BusinessException(ResultCode.CONFLICT, "脚本已经存在测试集，无法删除");
        }
        scriptService.delete(form.getEntityIds());
    }

    @ApiOperation(value="脚本分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/scriptPage")
    public PageResult scriptPage(@PathVariable Long projectId, @Valid ScriptListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        return scriptService.findPage(projectId, queryForm);
    }

    @ApiOperation(value="脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/scriptList")
    public List scriptList(@PathVariable Long projectId, @Valid ScriptListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        return scriptService.findList(projectId, queryForm);
    }

    @ApiOperation(value="获得脚本内容", notes="")
    @ResponseBody
    @GetMapping(value = "/script/read/{scriptId}")
    public Result read(@PathVariable Long scriptId) throws IOException{
        validator.validateScriptExist(scriptId);
        String content = scriptService.getContent(scriptId);
        return Result.success(content);
    }

    @ApiOperation(value="修改脚本内容", notes="")
    @ResponseBody
    @PostMapping(value = {"/script/write/{scriptId}"})
    public void write(@PathVariable Long scriptId, @RequestBody ScriptContentForm form) throws IOException {
        validator.validateScriptExist(scriptId);
        scriptService.modifyContent(scriptId, form.getContent());
    }


}
