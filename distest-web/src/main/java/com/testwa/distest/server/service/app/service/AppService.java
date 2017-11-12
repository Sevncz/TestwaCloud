package com.testwa.distest.server.service.app.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.utils.*;
import com.testwa.distest.common.android.TestwaAndroidApp;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.exception.NoSuchAppException;
import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.mvc.beans.PageResult;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long appId){
        appDAO.delete(appId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> appIds){
        appDAO.delete(appIds);
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
        update(app);

    }

    public App findOne(Long appId){
        return appDAO.findOne(appId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public App upload(MultipartFile uploadfile, AppNewForm form) throws IOException {

        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        Path dir = Paths.get(disFileProperties.getApp(), Identities.uuid2());
        Files.createDirectories(dir);
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.write(filepath, uploadfile.getBytes(), StandardOpenOption.CREATE);

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        return saveFile(filename, aliasName, filepath.toString(), size, type, form);

    }
    private App saveFile(String filename, String aliasName, String filepath, String size, String type, AppNewForm form) throws IOException {
        App app = new App();

        switch (type.toLowerCase()){
            case "apk":
                app.setOsType(DB.PhoneOS.ANDROID);
                TestwaAndroidApp androidApp = new TestwaAndroidApp(new File(filepath));
                app.setActivity(androidApp.getMainActivity());
                app.setPackageName(androidApp.getBasePackage());
                app.setSdkVersion(androidApp.getSdkVersion());
                app.setTargetSdkVersion(androidApp.getTargetSdkVersion());
                break;
            case "zip":
                app.setOsType(DB.PhoneOS.IOS);
                String unzipPath = filepath.substring(0, filepath.lastIndexOf(".") - 4);
                filename = filename.substring(0, filename.lastIndexOf("."));
                ZipUtil.unZipFiles(filepath, unzipPath);
                aliasName = Paths.get(unzipPath.substring(unzipPath.lastIndexOf(File.separator) + 1), filename).toString();
                break;
            case "ipa":
                app.setOsType(DB.PhoneOS.IOS);
                break;
            default:
                app.setOsType(DB.PhoneOS.UNKNOWN);
                break;

        }

        app.setAliasName(aliasName);
        app.setAppName(filename);
        app.setPath(filepath);
        app.setCreateTime(new Date());
        app.setSize(size);
        app.setMd5(IOUtil.fileMD5(filepath));
        if(form != null){

            app.setProjectId(form.getProjectId());
            app.setDescription(form.getDescription());
            app.setVersion(form.getVersion());
        }
        User currentUser = userService.findByUsername(getCurrentUsername());
        app.setCreateBy(currentUser.getId());
        appDAO.insert(app);
        return app;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public App upload(MultipartFile uploadfile) throws IOException, AccountException {
        return upload(uploadfile, null);
    }

    public PageResult<App> findByPage(App app, int page, int rows){
        //分页处理
        PageHelper.startPage(page, rows);
        List<App> userList = appDAO.findBy(app);
        PageInfo info = new PageInfo(userList);
        PageResult<App> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

//    public List<App> find(List<String> projectIds, String appName) {
//        List<Criteria> andCriteria = new ArrayList<>();
//        if(StringUtils.isNotEmpty(appName)){
//            andCriteria.add(Criteria.where("name").regex(appName));
//        }
//        andCriteria.add(Criteria.where("projectId").in(projectIds));
//        andCriteria.add(Criteria.where("disable").is(false));
//
//        Query query = buildQueryByCriteria(andCriteria, null);
//
//        return appDAO.find(query);
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


    public PageResult<App> findPage(AppListForm queryForm) throws AccountException {
        //分页处理
        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        List<App> appList = find(queryForm);
        PageInfo<App> info = new PageInfo(appList);
        PageResult<App> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<App> find(AppListForm queryForm) throws AccountException {
        Map<String, Object> params = buildQueryParams(queryForm);
        List<App> appList = appDAO.findByFromProject(params);
        return appList;
    }

    private Map<String, Object> buildQueryParams(AppListForm queryForm) throws AccountException {
        List<Project> projects = projectService.findAllOfUserProject(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", queryForm.getProjectId());
        params.put("appName", queryForm.getAppName());
        params.put("projects", projects);
        return params;
    }

    public List<App> findAll(List<Long> entityIds) {
        return appDAO.findAll(entityIds);
    }

}
