package com.testwa.distest.server.web.script.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.service.script.form.ScriptContentForm;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * Created by wen on 16/9/2.
 */
@Api("脚本相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/script")
public class ScriptController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ScriptController.class);

    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ScriptValidator validator;
    @Autowired
    private FileUploadValidator fileUploadValidator;

    @ApiOperation(value="补充脚本信息", notes="")
    @ResponseBody
    @PostMapping(value = "/save")
    public Result appendInfo(@Valid ScriptUpdateForm form) throws AccountException, NoSuchScriptException {
        scriptService.update(form);
        return ok();
    }


    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/upload")
    public Result upload(@RequestParam("file") MultipartFile uploadfile) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        long fileSize = 1024 * 1024 * 400;
        String[] allowExtName = {".apk", ".ipa", ".zip"};
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);

        Script script = scriptService.upload(uploadfile);
        ScriptVO vo = new ScriptVO();
        BeanUtils.copyProperties(script, vo);
        return ok(vo);
    }

    @ApiOperation(value="上传多个脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/upload/multi")
    public Result uploadMulti(@RequestParam("file") List<MultipartFile> uploadfiles) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        long fileSize = 1024 * 1024 * 400;
        String[] allowExtName = {".apk", ".ipa", ".zip"};
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles);
        return ok();
    }


    @ApiOperation(value="删除脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/delete")
    public Result delete(@Valid DeleteAllForm form) {

        scriptService.delete(form.getEntityIds());
        return ok();
    }


    @ApiOperation(value="脚本分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(ScriptListForm queryForm) throws NotInProjectException, AccountException {
        PageResult<Script> scriptPageResult = scriptService.findPage(queryForm);
        List<ScriptVO> vos = buildVOs(scriptPageResult.getPages(), ScriptVO.class);
        PageResult<ScriptVO> pr = new PageResult<>(vos, scriptPageResult.getTotal());
        return ok(pr);
    }


    @ApiOperation(value="获得脚本内容", notes="")
    @ResponseBody
    @GetMapping(value = "/read/{scriptId}")
    public Result read(@PathVariable Long scriptId) throws NoSuchScriptException, IOException {
        validator.validateScriptExist(scriptId);
        String content = scriptService.getContent(scriptId);
        return ok(content);
    }


    @ApiOperation(value="修改脚本内容", notes="")
    @ResponseBody
    @PostMapping(value = {"/write/{scriptId}"})
    public Result write(@PathVariable Long scriptId, @RequestBody ScriptContentForm form) throws AccountException, IOException, NoSuchScriptException {
        validator.validateScriptExist(scriptId);
        scriptService.modifyContent(scriptId, form.getContent());
        return ok();
    }

    @ApiOperation(value="脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(ScriptListForm queryForm) throws NotInProjectException, AccountException {
        List<Script> scriptList = scriptService.find(queryForm);
        List<ScriptVO> vos = buildVOs(scriptList, ScriptVO.class);
        return ok(vos);
    }

}
