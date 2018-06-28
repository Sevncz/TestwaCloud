package com.testwa.distest.server.web.app.controller;

import com.testwa.core.base.form.IDForm;
import com.testwa.core.base.vo.Result;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.form.AppInstallForm;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.mgr.InstallMgr;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.app.vo.AppVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("应用操作相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/app")
public class AppController extends BaseController {
    private static final String[] allowExtName = {".apk", ".ipa", ".zip"};;
    private static final long fileSize = 1024 * 1024 * 400;  // 100k

    @Autowired
    private AppService appService;
    @Autowired
    private AppInfoService appInfoService;
    @Autowired
    private UserService userService;
    @Autowired
    private InstallMgr installMgr;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private FileUploadValidator fileUploadValidator;

    @ApiOperation(value="上传应用，不在项目中", notes="")
    @ResponseBody
    @PostMapping(value="/upload", consumes = "multipart/form-data")
    public Result upload(@RequestParam("appfile") MultipartFile appfile) throws IOException, AccountException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        fileUploadValidator.validateFile(appfile, fileSize, allowExtName);

        App app = appService.upload(appfile);
        AppVO vo = buildVO(app, AppVO.class);
        return ok(vo);
    }

    @ApiOperation(value="上传应用", notes="")
    @ResponseBody
    @PostMapping(value="/upload/only/{projectId}", consumes = "multipart/form-data")
    public Result uploadOnly(@RequestParam("appfile") MultipartFile appfile, @PathVariable Long projectId) throws IOException, AccountException, ParamsIsNullException, ParamsFormatException {
        //
        // 校验
        //
        fileUploadValidator.validateFile(appfile, fileSize, allowExtName);
        projectValidator.validateProjectExist(projectId);

        App app = appService.uploadOnly(appfile, projectId);
        AppVO vo = buildVO(app, AppVO.class);
        return ok(vo);
    }

    @ApiOperation(value="更新应用信息", notes="在upload之后调用，用于补充app的信息")
    @ResponseBody
    @PostMapping(value = "/append")
    public Result appendInfo(@Valid @RequestBody AppUpdateForm form) throws ObjectNotExistsException, AuthorizedException {
        appValidator.validateAppExist(form.getAppId());
        projectValidator.validateProjectExist(form.getProjectId());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());
        appService.appendInfo(form);
        return ok();
    }

    @ApiOperation(value="安装应用", notes="")
    @ResponseBody
    @PostMapping(value = "/install")
    public Result install(@RequestBody AppInstallForm appInstallForm) throws ParamsIsNullException {
        if( appInstallForm.getAppId() == null || appInstallForm.getDeviceIds() == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        appValidator.validateAppExist(appInstallForm.getAppId());
        installMgr.install(appInstallForm);
        return ok();
    }

    @ApiOperation(value="卸载应用", notes="")
    @ResponseBody
    @PostMapping(value = "/uninstall")
    public Result uninstall(@RequestBody AppInstallForm appInstallForm) throws ParamsIsNullException {
        if( appInstallForm.getAppId() == null || appInstallForm.getDeviceIds() == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        appValidator.validateAppExist(appInstallForm.getAppId());
        installMgr.uninstall(appInstallForm);
        return ok();
    }

    @ApiOperation(value="app分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/page")
    public Result page(@PathVariable Long projectId, @Valid AppListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());
        PageResult<AppInfo> pr = appInfoService.findPage(projectId, queryForm);
        return ok(pr);
    }

    @ApiOperation(value="app列表", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/list")
    public Result list(@PathVariable Long projectId, @Valid AppListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());
        List<AppInfo> apps = appInfoService.findList(projectId, queryForm);
        return ok(apps);
    }

    @ApiOperation(value="查询一个App", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/one/{query:.+}")
    public Result queryOne(@PathVariable("projectId") Long projectId, @PathVariable("query") String query) {
        projectValidator.validateProjectExist(projectId);
        AppInfo appInfo = appInfoService.getByQuery(projectId, query);
        return ok(appInfo);
    }

    @ApiOperation(value="删除多个应用", notes="")
    @ResponseBody
    @PostMapping(value = "/delete/all")
    public Result deleteAll(@RequestBody IDListForm del) throws ParamsIsNullException {
        if(del.getEntityIds() == null && del.getEntityIds().size() == 0){
            throw new ParamsIsNullException("参数不能为空");
        }
        appInfoService.deleteAll(del.getEntityIds());
        return ok();
    }

    @ApiOperation(value="删除一个应用", notes="")
    @ResponseBody
    @PostMapping(value = "/delete/one")
    public Result deleteOne(@RequestBody IDForm del) throws ParamsIsNullException {
        if( del.getEntityId() == null ){
            throw new ParamsIsNullException("参数不能为空");
        }
        appInfoService.delete(del.getEntityId());
        return ok();
    }

    @ApiOperation(value="该app所有上传的版本", notes="")
    @ResponseBody
    @GetMapping(value = "/{appinfoId}/version/list")
    public Result versionList(@PathVariable("appinfoId") Long appinfoId) {
        AppInfo appInfo = appValidator.validateAppInfoExist(appinfoId);
        List<App> apps = appService.getAllVersions(appInfo);
        return ok(apps);
    }

}
