package com.testwa.distest.server.service;

import com.testwa.distest.server.android.TestwaAndroidApp;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaProcedureInfo;
import com.testwa.distest.server.repository.TestwaAppRepository;
import com.testwa.distest.server.repository.TestwaReportRepository;
import com.testwa.distest.server.repository.TestwaScriptRepository;
import com.testwa.distest.server.repository.TestwaTestcaseRepository;
import com.testwa.distest.server.util.AppType;
import com.testwa.distest.server.util.IOUtil;
import com.testwa.distest.server.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class TestwaAppService extends BaseService {

    @Autowired
    private TestwaAppRepository appRepository;
    @Autowired
    private TestwaScriptRepository testwaScriptRepository;
    @Autowired
    private TestwaTestcaseRepository testwaTestcaseRepository;
    @Autowired
    private TestwaReportRepository testwaReportRepository;

    public void save(TestwaApp app){
        appRepository.save(app);
    }

    public void deleteById(String appId){
//        TestwaApp app = appRepository.findOne(appId);
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

        deleteAllRelatedObjectByProjectId(appId);
    }


    public void deleteAllRelatedObjectByProjectId(String appId){
        Query query = new Query();
        query.addCriteria(Criteria.where("appId").is(appId));

        Update update = new Update();
        update.set("disable", false);
        update.set("modifyDate", new Date());

        testwaScriptRepository.updateMulti(query, update);
        testwaTestcaseRepository.updateMulti(query, update);
        testwaReportRepository.updateMulti(query, update);
    }

    public void update(TestwaApp app) {
        appRepository.save(app);
    }

    public TestwaApp getAppById(String appId){
        return appRepository.findOne(appId);
    }

    public TestwaApp saveApp(String filename, String aliasName, String filepath, String size, String type) throws IOException {
        TestwaApp app = new TestwaApp();

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
        app.setDisable(false);
        app.setSize(size);
        app.setMd5(IOUtil.fileMD5(filepath));

        appRepository.save(app);
        return app;
    }

    public Page<TestwaApp> findAll(PageRequest pageRequest) {
        return appRepository.findAll(pageRequest);
    }

    public List<TestwaApp> findAll() {
        return appRepository.findAll();
    }

    public Page<TestwaApp> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return appRepository.find(query, pageRequest);
    }

    public List<TestwaApp> find(List<Map<String, String>> filters){
        Query query = buildQuery(filters);
        return appRepository.find(query);
    }

}
