package com.testwa.distest.server.web.app.controller;

import com.testwa.core.base.form.IDForm;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.form.AppInstallForm;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.web.app.mgr.AppInfoMgr;
import com.testwa.distest.server.web.app.mgr.InstallMgr;
import com.testwa.distest.server.web.app.validator.AppInfoValidator;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.app.vo.AppInfoVO;
import com.testwa.distest.server.web.app.vo.AppInfoVersionsDetailVO;
import com.testwa.distest.server.web.app.vo.AppVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class AppController extends BaseController {
    private static final String[] allowExtName = {".apk", ".ipa", ".zip"};;
    private static final long fileSize = 1024 * 1024 * 400;  // 100k

    @Autowired
    private AppService appService;
    @Autowired
    private AppInfoService appInfoService;
    @Autowired
    private AppInfoMgr appInfoMgr;
    @Autowired
    private InstallMgr installMgr;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private AppInfoValidator appInfoValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private FileUploadValidator fileUploadValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value="上传应用，不在项目中", notes="")
    @ResponseBody
    @PostMapping(value="/app/upload", consumes = "multipart/form-data")
    public AppVO upload(@RequestParam("appfile") MultipartFile appfile) throws IOException{
        //
        // 校验
        //
        fileUploadValidator.validateFile(appfile, fileSize, allowExtName);

        App app = appService.upload(appfile);
        return buildVO(app, AppVO.class);
    }

    @ApiOperation(value="上传应用", notes="")
    @ResponseBody
    @PostMapping(value="/project/{projectId}/app/uploadOnly", consumes = "multipart/form-data")
    public AppVO uploadOnly(@RequestParam("appfile") MultipartFile appfile, @PathVariable Long projectId) throws IOException {
        //
        // 校验
        //
        fileUploadValidator.validateFile(appfile, fileSize, allowExtName);
        projectValidator.validateProjectExist(projectId);

        App app = appService.uploadOnly(appfile, projectId);
        return buildVO(app, AppVO.class);
    }

    @ApiOperation(value="更新应用信息", notes="在upload之后调用，用于补充app的信息")
    @ResponseBody
    @PostMapping(value = "/app/append")
    public void appendInfo(@Valid @RequestBody AppUpdateForm form) {
        appValidator.validateAppExist(form.getAppId());
        projectValidator.validateProjectExist(form.getProjectId());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), currentUser.getId());
        appService.appendInfo(form);
    }

    @ApiOperation(value="安装应用", notes="")
    @ResponseBody
    @PostMapping(value = "/app/install")
    public void install(@RequestBody @Valid AppInstallForm appInstallForm) {
        appValidator.validateAppExist(appInstallForm.getAppId());
        installMgr.install(appInstallForm);
    }

    @ApiOperation(value="卸载应用", notes="")
    @ResponseBody
    @PostMapping(value = "/app/uninstall")
    public void uninstall(@RequestBody @Valid AppInstallForm appInstallForm) {
        appValidator.validateAppExist(appInstallForm.getAppId());
        installMgr.uninstall(appInstallForm);
    }

    @ApiOperation(value="app分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/appPage")
    public PageResult appPage(@PathVariable Long projectId, @Valid AppListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        return appInfoService.page(projectId, queryForm);
    }

    @ApiOperation(value="app列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/appList")
    public List<AppInfo> appList(@PathVariable Long projectId, @Valid AppListForm queryForm) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        return appInfoService.list(projectId, queryForm);
    }

    @ApiOperation(value="搜索一个AppInfo", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/searchOneApp/{query:.+}")
    public AppInfoVO searchOneApp(@PathVariable("projectId") Long projectId, @PathVariable("query") String query) {
        projectValidator.validateProjectExist(projectId);
        AppInfo appInfo = appInfoService.getByQuery(projectId, query);
        return buildVO(appInfo, AppInfoVO.class);
    }

    @ApiOperation(value="获取一个App的详情", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/app/{appInfoId}/appDetail")
    public AppInfoVersionsDetailVO getDetail(@PathVariable("projectId") Long projectId, @PathVariable("appInfoId") Long appInfoId) {
        projectValidator.validateProjectExist(projectId);
        appInfoValidator.validateAppInfoExist(appInfoId);

        return appInfoMgr.getAppDetail(appInfoId);
    }

    @ApiOperation(value="删除多个应用", notes="")
    @ResponseBody
    @PostMapping(value = "/app/deleteAll")
    public void deleteAll(@RequestBody @Valid IDListForm del) {
        appInfoService.deleteAll(del.getEntityIds());
    }

    @ApiOperation(value="删除一个应用", notes="")
    @ResponseBody
    @PostMapping(value = "/app/deleteOne")
    public void deleteOne(@RequestBody @Valid IDForm del) {
        AppInfo appInfo = appInfoValidator.validateAppInfoExist(del.getEntityId());
        appInfoService.disableAppInfo(appInfo);
    }

    @ApiOperation(value="该app所有上传的版本", notes="")
    @ResponseBody
    @GetMapping(value = "/app/{appinfoId}/versionList")
    public List<AppVO> versionList(@PathVariable("appinfoId") Long appinfoId) {
        AppInfo appInfo = appInfoValidator.validateAppInfoExist(appinfoId);
        List<App> appList = appService.getAllVersions(appInfo);

        return buildVOs(appList, AppVO.class);
    }

}
