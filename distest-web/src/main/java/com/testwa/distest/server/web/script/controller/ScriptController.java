package com.testwa.distest.server.web.script.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsFormatException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptContentForm;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Api("脚本相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/script")
public class ScriptController extends BaseController {
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

    @ApiOperation(value="上传脚本", notes="")
    @ResponseBody
    @PostMapping(value = "/upload")
    public Result upload(@RequestParam("scriptfile") MultipartFile uploadfile) throws ParamsIsNullException, ParamsFormatException, IOException {
        //
        // 校验
        //
        long fileSize = 1024 * 100;  // 100k
        String[] allowExtName = {".java", ".js", ".py", ".rb"};  // 这里必须是有序数组，字母顺序
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);

        Script script = scriptService.upload(uploadfile);
        ScriptVO vo = new ScriptVO();
        BeanUtils.copyProperties(script, vo);
        return ok(vo);
    }

    /**
     * 可以上传一组脚本形成案例，暂时还不用
     * @param uploadfiles
     * @return
     * @throws IOException
     * @throws ParamsIsNullException
     * @throws ParamsFormatException
     */
    @ApiOperation(value="上传多个脚本", notes="暂时还不用")
    @ResponseBody
    @PostMapping(value = "/upload/multi")
    public Result uploadMulti(@RequestParam("file") List<MultipartFile> uploadfiles) throws IOException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        long fileSize = 1024 * 1024 * 400;
        String[] allowExtName = {".java", ".js", ".py", ".rb"};  // 这里必须是有序数组，字母顺序
        fileUploadValidator.validateFiles(uploadfiles, fileSize, allowExtName);
        scriptService.uploadMulti(uploadfiles);
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
    public Result delete(@RequestBody @Valid DeleteAllForm form) {

        scriptService.deleteScript(form.getEntityIds());
        return ok();
    }


    @ApiOperation(value="脚本分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid ScriptListForm queryForm) throws AuthorizedException {
        if(queryForm.getProjectId() != null){
            User user = userService.findByUsername(WebUtil.getCurrentUsername());
            projectValidator.validateUserIsProjectMember(queryForm.getProjectId(), user.getId());
        }
        PageResult<Script> scriptPageResult = scriptService.findPageForCurrentUser(queryForm);
        List<ScriptVO> vos = buildVOs(scriptPageResult.getPages(), ScriptVO.class);
        PageResult<ScriptVO> pr = new PageResult<>(vos, scriptPageResult.getTotal());
        return ok(pr);
    }

    @ApiOperation(value="脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(@Valid ScriptListForm queryForm) throws AuthorizedException {
        if(queryForm.getProjectId() != null){
            User user = userService.findByUsername(WebUtil.getCurrentUsername());
            projectValidator.validateUserIsProjectMember(queryForm.getProjectId(), user.getId());
        }
        List<Script> scriptList = scriptService.findForCurrentUser(queryForm);
        List<ScriptVO> vos = buildVOs(scriptList, ScriptVO.class);
        return ok(vos);
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
