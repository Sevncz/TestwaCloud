package com.testwa.distest.server.service.app.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.common.android.AndroidOSInfo;
import com.testwa.distest.common.android.TestwaAndroidApp;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.AppUtil;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.dao.IAppDAO;
import com.testwa.distest.server.service.app.dao.IAppInfoDAO;
import com.testwa.distest.server.service.app.form.AppListForm;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;


/**
 * Created by wen on 16/9/1.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AppInfoService {
    private static final String REG_ANDROID_PACKAGE = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*";
    @Autowired
    private IAppDAO appDAO;
    @Autowired
    private IAppInfoDAO appInfoDAO;
    @Autowired
    private ProjectService projectService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long entityId){
        AppInfo appInfo = appInfoDAO.findOne(entityId);

        appInfoDAO.disableApp(entityId);
        appDAO.disableAll(appInfo.getPackageName(), appInfo.getProjectId());
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteAll(List<Long> entityIds){
        entityIds.forEach(this::delete);
    }

    public AppInfo findOne(Long entityId){
        return appInfoDAO.findOne(entityId);
    }

    public AppInfo findOneInProject(Long entityId, Long projectId) {
        return appInfoDAO.findOneInProject(entityId, projectId);
    }

    public List<AppInfo> findList(Long projectId, AppListForm queryForm) {
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getAppName())) {
            query.setName(queryForm.getAppName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            query.setPackageName(queryForm.getPackageName());
        }
        return appInfoDAO.findBy(query);
    }

    public PageResult<AppInfo> findPage(Long projectId, AppListForm queryForm){
        //分页处理
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getAppName())) {
            query.setName(queryForm.getAppName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            query.setPackageName(queryForm.getPackageName());
        }
        int offset = 0;
        if(queryForm.getPageNo() >= 1) {
            offset = (queryForm.getPageNo() - 1) * queryForm.getPageSize();
        }
        List<AppInfo> appList = appInfoDAO.findPage(query, queryForm.getOrderBy(), queryForm.getOrder(), offset, queryForm.getPageSize());
        Long total = appInfoDAO.countBy(query);
        PageResult<AppInfo> pr = new PageResult<>(appList, total);
        return pr;
    }

    public List<AppInfo> findByProjectId(Long projectId) {
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        List<AppInfo> result = appInfoDAO.findBy(query);
        return result;
    }

    public List<AppInfo> findAll(List<Long> entityIds) {
        return appInfoDAO.findAll(entityIds);
    }

    public AppInfo getByPackage(Long projectId, String packageName) {
        return appInfoDAO.getByPackage(projectId, packageName);
    }

    public AppInfo getByQuery(Long projectId, String query) {
        Pattern r = Pattern.compile(REG_ANDROID_PACKAGE);
        Matcher matcher = r.matcher(query);
        if (matcher.find()) {
            return appInfoDAO.getByPackage(projectId, query);
        }
        return appInfoDAO.getByName(projectId, query);
    }

}
