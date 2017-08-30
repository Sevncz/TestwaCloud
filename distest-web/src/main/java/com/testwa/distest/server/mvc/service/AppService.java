package com.testwa.distest.server.mvc.service;

import com.testwa.core.utils.AppType;
import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.ZipUtil;
import com.testwa.distest.server.android.TestwaAndroidApp;
import com.testwa.distest.server.exception.NoSuchAppException;
import com.testwa.distest.server.mvc.model.App;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import com.testwa.distest.server.mvc.repository.AppRepository;
import com.testwa.distest.server.mvc.repository.ReportRepository;
import com.testwa.distest.server.mvc.repository.ScriptRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import com.testwa.distest.server.mvc.vo.AppVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class AppService extends BaseService {

    @Autowired
    private AppRepository appRepository;
    @Autowired
    private ScriptRepository scriptRepository;
    @Autowired
    private TestcaseRepository testcaseRepository;
    @Autowired
    private ReportRepository reportRepository;

    public void save(App app){
        appRepository.save(app);
    }

    public void deleteById(String appId){
//        App app = appRepository.findOne(appId);
//        String filepath = app.getPath();
//        Path path = Paths.get(filepath);
//        try {
//            Files.deleteIfExists(path);
//            Files.deleteIfExists(path.getParent());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        appRepository.delete(appId);

        disableById(appId, appRepository);

        disableAllRelatedObjectByProjectId(appId);
    }


    public void disableAllRelatedObjectByProjectId(String appId){
        Query query = new Query();
        query.addCriteria(Criteria.where("appId").is(appId));

        Update update = new Update();
        update.set("disable", true);
        update.set("modifyDate", new Date());

        scriptRepository.updateMulti(query, update);
        testcaseRepository.updateMulti(query, update);
        reportRepository.updateMulti(query, update);
    }

    public void update(App app) {
        appRepository.save(app);
    }

    public App getAppById(String appId){
        return appRepository.findOne(appId);
    }

    public void checkApp(String appId) throws NoSuchAppException{
        App app = this.getAppById(appId);
        if ( null == app) {
            throw new NoSuchAppException("无此应用！");
        }
    }
    public App saveApp(String filename, String aliasName, String filepath, String size, String type) throws IOException {
        App app = new App();

        switch (type.toLowerCase()){
            case "apk":
                app.setType(AppType.ANDROID.getName());
                TestwaAndroidApp androidApp = new TestwaAndroidApp(new File(filepath));
                app.setActivity(androidApp.getMainActivity());
                app.setPackageName(androidApp.getBasePackage());
                app.setSdkVersion(androidApp.getSdkVersion());
                app.setTargetSdkVersion(androidApp.getTargetSdkVersion());
                break;
            case "zip":
                app.setType(AppType.IOS.getName());
                String unzipPath = filepath.substring(0, filepath.lastIndexOf(".") - 4);
                filename = filename.substring(0, filename.lastIndexOf("."));
                ZipUtil.unZipFiles(filepath, unzipPath);
                aliasName = Paths.get(unzipPath.substring(unzipPath.lastIndexOf(File.separator) + 1), filename).toString();
                break;
            case "ipa":
                app.setType(AppType.IOS.getName());
                break;
            default:
                app.setType(AppType.OTHER.getName());
                break;

        }

        app.setAliasName(aliasName);
        app.setName(filename);
        app.setPath(filepath);
        app.setCreateDate(new Date());
        app.setDisable(true);
        app.setSize(size);
        app.setMd5(IOUtil.fileMD5(filepath));

        appRepository.save(app);
        return app;
    }

    public Page<App> findAll(PageRequest pageRequest) {
        return appRepository.findAll(pageRequest);
    }

    public List<App> findAll() {
        return appRepository.findAll();
    }

    public List<App> find(List<String> projectIds, String appName) {
        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(appName)){
            andCriteria.add(Criteria.where("name").regex(appName));
        }
        andCriteria.add(Criteria.where("projectId").in(projectIds));
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);

        return appRepository.find(query);

    }

    public Page<App> findPage(PageRequest pageRequest, List<String> projectIds, String appName) {

        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(appName)){
            andCriteria.add(Criteria.where("name").regex(appName));
        }
        andCriteria.add(Criteria.where("projectId").in(projectIds));
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);
        return appRepository.find(query, pageRequest);
    }

    public AppVO getAppVOById(String appId) {
        AppVO appVO = new AppVO();
        App app = this.getAppById(appId);
        if (app != null) {
            BeanUtils.copyProperties(app, appVO);
        }
        return appVO;
    }
}
