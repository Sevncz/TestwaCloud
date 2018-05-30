package com.testwa.distest.server.service.app.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.*;
import com.testwa.distest.common.android.AndroidOSInfo;
import com.testwa.distest.common.android.TestwaAndroidApp;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.AppUtil;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.dao.IAppDAO;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppNewForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.vo.AppVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;


/**
 * Created by wen on 16/9/1.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AppService {
    @Autowired
    private IAppDAO appDAO;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private DisFileProperties disFileProperties;

    /**
     * 只删除记录
     * @param appId
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long appId){
        appDAO.delete(appId);
    }

    /**
     * 删除app及其文件
     * @param appId
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteApp(Long appId){
        App app = findOne(appId);
        if (app == null){
            return;
        }

        String filePath = disFileProperties.getApp() + File.separator + app.getPath();
        String iconPath = disFileProperties.getApp() + File.separator + app.getIcon();
        try {
            // 删除app文件
            Files.deleteIfExists(Paths.get(filePath));
            // 删除icon
            Files.deleteIfExists(Paths.get(iconPath));
            // 删除文件夹
            Files.deleteIfExists(Paths.get(filePath).getParent());
        } catch (IOException e) {
            log.error("delete app file error", e);
        }
        // 删除记录
        delete(appId);

    }

    /**
     * 删除多条记录
     * @param appIds
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> appIds){
        appDAO.delete(appIds);
    }

    /**
     * 删除多个app及其文件
     * @param appIds
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteApp(List<Long> appIds){
        appIds.forEach(this::deleteApp);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(App app) {
        appDAO.update(app);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(AppUpdateForm form) {

        User currentUser = userService.findByUsername(getCurrentUsername());
        App app = findOne(form.getAppId());
        app.setProjectId(form.getProjectId());
        app.setVersion(form.getVersion());
        app.setCreateBy(currentUser.getId());
        app.setDescription(form.getDescription());
        app.setUpdateTime(new Date());
        app.setUpdateBy(currentUser.getId());
        update(app);

    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void appendInfo(AppUpdateForm form) {
        User currentUser = userService.findByUsername(getCurrentUsername());
        App app = findOne(form.getAppId());
        app.setProjectId(form.getProjectId());
        app.setVersion(form.getVersion());
        app.setDescription(form.getDescription());
        app.setUpdateTime(new Date());
        app.setUpdateBy(currentUser.getId());
        app.setEnabled(true);
        update(app);
    }

    public App findOne(Long entityId){
        return appDAO.findOne(entityId);
    }

    public App findOneInProject(Long entityId, Long projectId) {
        return appDAO.findOneInProject(entityId, projectId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public App uploadOnly(MultipartFile uploadfile, Long projectId) throws IOException {

        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        String dirName = Identities.uuid2();
        Path dir = Paths.get(disFileProperties.getApp(), dirName);
        Files.createDirectories(dir);
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.write(filepath, uploadfile.getBytes(), StandardOpenOption.CREATE);

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        String md5 = IOUtil.fileMD5(filepath.toString());
        List<App> appList = findByMd5InProject(md5, projectId);
        AppNewForm form = new AppNewForm();
        form.setProjectId(projectId);
        if(appList == null || appList.size() == 0){
            return saveFile(filename, aliasName, filepath.toString(), dirName, size, type, md5, form);
        }
        return appList.get(0);
    }

    private List<App> findByMd5InProject(String md5, Long projectId) {
        App query = new App();
        query.setProjectId(projectId);
        query.setMd5(md5);
        List<App> appList = appDAO.findBy(query);
        return appList;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public App upload(MultipartFile uploadfile, AppNewForm form) throws IOException {

        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        String dirName = Identities.uuid2();
        Path dir = Paths.get(disFileProperties.getApp(), dirName);
        Files.createDirectories(dir);
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.write(filepath, uploadfile.getBytes(), StandardOpenOption.CREATE);

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        String md5 = IOUtil.fileMD5(filepath.toString());
        return saveFile(filename, aliasName, filepath.toString(), dirName, size, type, md5, form);

    }
    private App saveFile(String filename, String aliasName, String filepath, String dirName, String size, String type, String md5, AppNewForm form) throws IOException {
        App app = new App();

        String relativePath = dirName + File.separator + aliasName;
        switch (type.toLowerCase()){
            case "apk":
                app.setPlatform(DB.PhoneOS.ANDROID);
                TestwaAndroidApp androidApp = new TestwaAndroidApp(new File(filepath));
                app.setActivity(androidApp.getMainActivity());
                app.setPackageName(androidApp.getBasePackage());
                app.setSdkBuild(androidApp.getTargetSdkVersion());
                app.setPlatformVersion(AndroidOSInfo.getOSVersionFromSDKLevel(androidApp.getTargetSdkVersion()));
                app.setMiniOSVersion(AndroidOSInfo.getOSVersionFromSDKLevel(androidApp.getSdkVersion()));
                app.setVersion(androidApp.getVersionName());
                String lableName = androidApp.getDisplayName();
                app.setDisplayName(lableName);
                Path dir = Paths.get(filepath).getParent();
                String iconPath = dir + File.separator + "icon.png";
                try {
                    String iconName = androidApp.getIcon();
                    if(StringUtils.isNotEmpty(iconName)){
                        AppUtil.extractFileFromApk(filepath, iconName, iconPath);
                        app.setIcon(dirName + File.separator + "icon.png");
                    }
                } catch (Exception e) {
                    log.error("android 图标文件解析失败：", e);
                }
                break;
            case "zip":
                String ipaname = filepath.replaceAll(".zip", ".ipa");
                Files.copy(Paths.get(filepath), Paths.get(ipaname));
                getIpaInfo(app, filepath, dirName);
                break;
            case "ipa":
                getIpaInfo(app, filepath, dirName);
                break;
            default:
                app.setPlatform(DB.PhoneOS.UNKNOWN);
                break;

        }
        app.setFileAliasName(aliasName);
        app.setFileName(filename);
        app.setPath(relativePath);
        app.setCreateTime(new Date());
        app.setSize(size);
        app.setMd5(md5);
        if(form != null){
            app.setProjectId(form.getProjectId());
            app.setDescription(form.getDescription());
            app.setEnabled(true);
        }
        User currentUser = userService.findByUsername(getCurrentUsername());
        app.setCreateBy(currentUser.getId());
        long appId = appDAO.insert(app);
        app.setId(appId);
        return app;
    }

    private void getIpaInfo(App app, String filePath, String dirName) {
        File ipaFile = new File(filePath);
        Map<String, String> ipaProperties = AppUtil.getIpaInfo(ipaFile);
        if(ipaProperties != null){
            String displayName = ipaProperties.getOrDefault("CFBundleDisplayName", "");
            app.setDisplayName(displayName);

            String version = ipaProperties.getOrDefault("CFBundleShortVersionString", "");
            app.setVersion(version);

            String minimumOSVersion = ipaProperties.getOrDefault("MinimumOSVersion", "");
            app.setMiniOSVersion(minimumOSVersion);

            String sdkBuild = ipaProperties.getOrDefault("DTSDKBuild", "");
            app.setSdkBuild(sdkBuild);

            String icon = ipaProperties.getOrDefault("icon", "");
            app.setIcon(dirName + File.separator + icon);

            String packageName = ipaProperties.getOrDefault("CFBundleIdentifier", "");
            app.setPackageName(packageName);

            String platformVersion = ipaProperties.getOrDefault("DTPlatformVersion", "");
            app.setPlatformVersion(platformVersion);

        }
        app.setPlatform(DB.PhoneOS.IOS);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public App upload(MultipartFile uploadfile) throws IOException{
        return upload(uploadfile, null);
    }

    public PageResult<App> findPage(App app, int page, int rows){
        //分页处理
        PageHelper.startPage(page, rows);
        List<App> appList = appDAO.findBy(app);
        PageInfo info = new PageInfo(appList);
        PageResult<App> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public PageResult<App> findPage(AppListForm queryForm){
        //分页处理
        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        App query = new App();
        query.setProjectId(queryForm.getProjectId());
        query.setDisplayName(queryForm.getAppName());
        List<App> appList = appDAO.findBy(query);
        PageInfo info = new PageInfo(appList);
        PageResult<App> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

//    public List<App> findForCurrentUser(List<String> projectIds, String appName) {
//        List<Criteria> andCriteria = new ArrayList<>();
//        if(StringUtils.isNotEmpty(appName)){
//            andCriteria.add(Criteria.where("name").regex(appName));
//        }
//        andCriteria.add(Criteria.where("projectId").in(projectIds));
//        andCriteria.add(Criteria.where("disableAll").is(false));
//
//        Query query = buildQueryByCriteria(andCriteria, null);
//
//        return appDAO.findForCurrentUser(query);
//
//    }

    public AppVO getAppVO(Long appId) {
        AppVO appVO = new AppVO();
        App app = this.findOne(appId);
        if (app != null) {
            BeanUtils.copyProperties(app, appVO);
        }
        return appVO;
    }

    public List<App> findByProjectId(Long projectId) {
        App query = new App();
        query.setProjectId(projectId);
        List<App> apps = appDAO.findBy(query);
        return apps;
    }

    public List<App> findAll(List<Long> entityIds) {
        return appDAO.findAll(entityIds);
    }

    /**
     * 获得用户自己可见的app分页列表
     * @param queryForm
     * @return
     */
    public PageResult<App> findPageForCurrentUser(AppListForm queryForm){
        //分页处理
        Map<String, Object> params = buildProjectParamsForCurrentUser(queryForm);
        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        List<App> appList = appDAO.findByFromProject(params);
        PageInfo<App> info = new PageInfo(appList);
        PageResult<App> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    /**
     * 获得用户自己可见的app列表
     * @param queryForm
     * @return
     */
    public List<App> findForCurrentUser(AppListForm queryForm){
        Map<String, Object> params = buildProjectParamsForCurrentUser(queryForm);
        List<App> appList = appDAO.findByFromProject(params);
        return appList;
    }

    private Map<String, Object> buildProjectParamsForCurrentUser(AppListForm queryForm){
        List<Project> projects = projectService.findAllByUserList(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        if(StringUtils.isNotEmpty(queryForm.getAppName())){
            params.put("displayName", queryForm.getAppName());
        }
        if(queryForm.getProjectId() != null){
            params.put("projectId", queryForm.getProjectId());
        }
        if(projects != null & projects.size() > 0){
            params.put("projects", projects);
        }
        return params;
    }
}
