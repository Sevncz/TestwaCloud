package com.testwa.distest.server.mvc.service;

import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.ScriptType;
import com.testwa.distest.server.exception.NoSuchScriptException;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.repository.ProjectRepository;
import com.testwa.distest.server.mvc.repository.ScriptRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class ScriptService extends BaseService{

    @Autowired
    private ScriptRepository scriptRepository;
    @Autowired
    private TestcaseRepository testcaseRepository;

    public void save(Script script){
        scriptRepository.save(script);
    }

    public void deleteById(String scriptId){
//        Script script = scriptRepository.findOne(scriptId);
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
        update.set("disable", true);
        update.set("modifyDate", new Date());

        testcaseRepository.updateMulti(query, update);
    }

    public Script getScriptById(String scriptId){
        return scriptRepository.findOne(scriptId);
    }

    public Script saveScript(String filename, String aliasName, String filepath, String size, String type) throws IOException {
        Script script = new Script();

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

    public Page<Script> findAll(PageRequest pageRequest) {

        return scriptRepository.findAll(pageRequest);
    }

    public List<Script> find(List<String> projectIds, String name) {
        Query query = buildQuery(projectIds, name);
        return scriptRepository.find(query);
    }

    public Page<Script> findPage(PageRequest pageRequest, List<String> projectIds, String scriptName) {
        Query query = buildQuery(projectIds, scriptName);
        return scriptRepository.find(query, pageRequest);
    }

    protected void checkScripts(List<String> scriptIds) throws NoSuchScriptException {
        List<Script> scripts = this.findScriptList(scriptIds);
        if (scripts.size() != scriptIds.size()) {
            throw new NoSuchScriptException("没有此脚本!");
        }
    }

    protected List<Script> findScriptList(List<String> scripts) {
        // 修复返回列表不按照顺序的bug 使用逐个获取策略
        List<Script> scriptList = new ArrayList<>();
        scripts.forEach(scriptId -> {
            Script script = scriptRepository.findOne(scriptId);
            scriptList.add(script);
        });
        return scriptList;
    }

    public List<Script> getScriptByProjectId(String projectId) {
        return scriptRepository.findByProjectId(projectId);
    }

    public Integer getCountScriptByProjectId(String projectId) {
        return scriptRepository.countByProjectId(projectId);
    }
}
