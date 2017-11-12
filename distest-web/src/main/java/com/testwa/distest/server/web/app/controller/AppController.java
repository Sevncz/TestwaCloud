package com.testwa.distest.server.web.app.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppNewForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.app.vo.AppVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@Api("应用操作相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/app")
public class AppController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private AppService appService;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private FileUploadValidator fileUploadValidator;

    @ApiOperation(value="上传应用", notes="一次性提交app的信息")
    @ResponseBody
    @PostMapping(value = "/save")
    public Result uploadFile(@Valid AppNewForm form, @RequestParam("appfile") MultipartFile file) throws AccountException, NoSuchAppException, NoSuchProjectException, ParamsIsNullException, ParamsFormatException, IOException {
        log.info(form.toString());
        //
        // 校验
        //
        long fileSize = 1024 * 1024 * 400;
        String[] allowExtName = {".apk", ".ipa", ".zip"};
        fileUploadValidator.validateFile(file, fileSize, allowExtName);
        App app = appService.upload(file, form);
        AppVO vo = buildVO(app, AppVO.class);

        return ok(vo);
    }

    @ApiOperation(value="上传应用", notes="")
    @ResponseBody
    @RequestMapping(value="/upload", method= RequestMethod.POST)
    public Result upload(@RequestParam("appfile") MultipartFile uploadfile) throws IOException, AccountException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        long fileSize = 1024 * 1024 * 400;
        String[] allowExtName = {".apk", ".ipa", ".zip"};
        fileUploadValidator.validateFile(uploadfile, fileSize, allowExtName);

        App app = appService.upload(uploadfile);
        AppVO vo = buildVO(app, AppVO.class);
        return ok(vo);
    }

    @ApiOperation(value="更新应用信息", notes="在upload之后调用，用于补充app的信息")
    @ResponseBody
    @PostMapping(value = "/append")
    public Result appendInfo(@Valid AppUpdateForm form) throws AccountException, NoSuchAppException, NoSuchProjectException {

        projectValidator.validateProjectExist(form.getProjectId());
        appService.update(form);
        return ok();
    }

    @ApiOperation(value="删除应用", notes="")
    @ResponseBody
    @PostMapping(value = "/delete")
    public Result delete(@RequestBody DeleteAllForm del){
        appService.delete(del.getEntityIds());
        return ok();
    }

    @ApiOperation(value="用户所有可见的app分页列表", notes="")
    @ResponseBody
    @RequestMapping(value = "/page", method= RequestMethod.GET)
    public Result page(@Valid AppListForm queryForm) throws ParamsIsNullException {
        PageResult<App> appPR = appService.findPageForCurrentUser(queryForm);
        PageResult<AppVO> pr = buildVOPageResult(appPR, AppVO.class);
        return ok(pr);

    }

    @ApiOperation(value="用户所有可见的app列表", notes="")
    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET, produces={"application/json"})
    public Result list(AppListForm queryForm) throws AccountException {
        List<App> apps = appService.findForCurrentUser(queryForm);
        List<AppVO> vos = buildVOs(apps, AppVO.class);
        return ok(vos);
    }

}
