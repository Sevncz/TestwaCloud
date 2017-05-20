package com.testwa.distest.server.service;

import com.testwa.distest.server.android.TestwaAndroidApp;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.TestwaReport;
import com.testwa.distest.server.model.TestwaScript;
import com.testwa.distest.server.repository.TestwaAppRepository;
import com.testwa.distest.server.repository.TestwaScriptRepository;
import com.testwa.distest.server.repository.TestwaTestcaseRepository;
import com.testwa.distest.server.util.AppType;
import com.testwa.distest.server.util.IOUtil;
import com.testwa.distest.server.util.ScriptType;
import com.testwa.distest.server.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
public class TestwaScriptService extends BaseService{

    @Autowired
    private TestwaScriptRepository scriptRepository;
    @Autowired
    private TestwaTestcaseRepository testwaTestcaseRepository;

    public void save(TestwaScript script){
        scriptRepository.save(script);
    }

    public void deleteById(String scriptId){
//        TestwaScript script = scriptRepository.findOne(scriptId);
//        if(script == null){
//            return;
//        }
//        String filepath = script.getPath();
//        Path path = Paths.get(filepath);
//        try {
//            Files.deleteIfExists(path);
//            Files.deleteIfExists(path.getParent());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        scriptRepository.delete(scriptId);


        disableById(scriptId, scriptRepository);

        deleteAllRelatedObjectByProjectId(scriptId);
    }

    private void deleteAllRelatedObjectByProjectId(String scriptId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("scriptId").in(scriptId));

        Update update = new Update();
        update.set("disable", false);
        update.set("modifyDate", new Date());

        testwaTestcaseRepository.updateMulti(query, update);
    }

    public TestwaScript getScriptById(String scriptId){
        return scriptRepository.findOne(scriptId);
    }

    public TestwaScript saveScript(String filename, String aliasName, String filepath, String size, String type) throws IOException {
        TestwaScript script = new TestwaScript();

        switch (type.toLowerCase()){
            case "py":
                script.setType(ScriptType.PYTHON.getName());
                break;
            case "java":
                script.setType(ScriptType.JAVA.getName());
                break;
            case "rb":
                script.setType(ScriptType.RUBY.getName());
                break;
            case "js":
                script.setType(ScriptType.JAVASCRIPT.getName());
                break;
            default:
                script.setType(ScriptType.OTHER.getName());
                break;
        }

        script.setCreateDate(new Date());
        script.setDisable(false);
        script.setName(filename);
        script.setPath(filepath);
        script.setAliasName(aliasName);
        script.setSize(size);
        script.setMd5(IOUtil.fileMD5(filepath));

        scriptRepository.save(script);
        return script;
    }

    public Page<TestwaScript> findAll(PageRequest pageRequest) {

        return scriptRepository.findAll(pageRequest);
    }

    public Page<TestwaScript> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return scriptRepository.find(query, pageRequest);
    }

}
